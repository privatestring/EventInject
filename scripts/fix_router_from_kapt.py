#!/usr/bin/env python3
"""
从 kapt _XXXxxx.java 反推正确的测试文件内容。
解析 kapt 文件中的：ROUTER_ACTION、所有 KEY 常量、方法签名中的参数（名称、nullable、desc）。
然后重新生成对应的测试 .kt 文件。
"""
import os, re

kapt_dir = "source/router/kapt"
test_dir = "app/src/main/java/com/joker/event/router"
OUTPUT_PKG = "com.joker.event.router"

# 从 kapt 文件中提取所有信息
def parse_kapt(filepath):
    content = open(filepath).read()
    
    # 类名
    m = re.search(r'public final class (\w+)_XXXxxx', content)
    if not m:
        return None
    class_name = m.group(1)
    
    # ROUTER_ACTION
    m = re.search(r'ROUTER_ACTION\s*=\s*"([^"]*)";', content)
    if not m:
        return None
    router_path = m.group(1)
    
    # KEY 常量: FIELD_CONST -> key_value
    keys = {}  # FIELD_CONST -> key_value
    for km in re.finditer(r'public static final String (\w+_INTENT_KEY)\s*=\s*"([^"]*)";', content):
        keys[km.group(1)] = km.group(2)
    
    # 从全参数版本的 jump 方法提取参数信息（第一个 jump 方法有最多参数）
    # 找到最长参数列表的 getActionScheme 方法的 javadoc
    params = []
    
    # 提取所有 @param 注释块
    javadoc_blocks = re.findall(r'/\*\*\s*\n((?:\s*\*[^\n]*\n)*)\s*\*/', content)
    
    # 找最长的 @param 列表
    best_params = []
    for block in javadoc_blocks:
        block_params = re.findall(r'@param\s+(\w+)\s+(.*)', block)
        if len(block_params) > len(best_params):
            best_params = block_params
    
    # 从方法签名中提取 nullable 信息
    # 找最长参数的 getActionScheme 方法签名
    scheme_methods = re.findall(r'public static String getActionScheme\(([^)]*)\)', content)
    longest_sig = ""
    for sig in scheme_methods:
        if len(sig) > len(longest_sig):
            longest_sig = sig
    
    # 解析参数签名
    nullable_map = {}  # param_name -> is_nullable
    if longest_sig:
        # 清理换行
        longest_sig = re.sub(r'\s+', ' ', longest_sig).strip()
        # 解析每个参数
        param_parts = re.split(r',\s*', longest_sig)
        for part in param_parts:
            part = part.strip()
            is_nullable = "@Nullable" in part
            # 提取参数名（最后一个词）
            name_match = re.search(r'(\w+)\s*$', part)
            if name_match:
                nullable_map[name_match.group(1)] = is_nullable
    
    # 构建参数列表
    # 用 KEY 常量反推字段名和 index
    # KEY 常量顺序就是 index 顺序
    key_list = list(keys.items())  # [(CONST_NAME, key_value), ...]
    
    # 从 best_params 获取 desc
    desc_map = {name: desc.strip() for name, desc in best_params}
    
    # 构建最终参数列表
    final_params = []
    for i, (const_name, key_value) in enumerate(key_list):
        # 从常量名反推字段名: USER_NAME_INTENT_KEY -> userName
        field_name_upper = const_name.replace("_INTENT_KEY", "")
        # UPPER_CASE -> camelCase
        parts = field_name_upper.lower().split("_")
        field_name = parts[0] + "".join(p.capitalize() for p in parts[1:])
        
        # 确认字段名在参数中
        # 有时候反推的名字和实际不完全匹配，用 desc_map 的 key 来校正
        actual_name = field_name
        if field_name not in desc_map and field_name not in nullable_map:
            # 尝试在 desc_map 中找匹配
            for dname in desc_map:
                if dname.upper().replace("_", "") == field_name_upper.replace("_", ""):
                    actual_name = dname
                    break
            # 还是找不到就用 nullable_map
            if actual_name == field_name:
                for nname in nullable_map:
                    if nname.upper().replace("_", "") == field_name_upper.replace("_", ""):
                        actual_name = nname
                        break
        
        is_nullable = nullable_map.get(actual_name, True)
        desc = desc_map.get(actual_name, "")
        
        # 判断 isOptional: 如果在最短版本的 jump 中不存在，则是 optional
        # 简化：nullable 且不是第一个参数 → 可能是 optional
        # 更准确：看是否有不包含该参数的 jump 重载
        is_optional = False
        # 检查是否存在不包含该参数的 getActionScheme 方法
        for sig in scheme_methods:
            sig_clean = re.sub(r'\s+', ' ', sig).strip()
            if actual_name not in sig_clean and len(sig_clean) > 0:
                is_optional = True
                break
        
        # 判断 key 是否是默认值（包名.字段名IntentKey）
        default_key = f"{OUTPUT_PKG}.{actual_name}IntentKey"
        custom_key = key_value if key_value != default_key else ""
        
        final_params.append({
            "name": actual_name,
            "index": i,
            "key": custom_key,
            "is_optional": is_optional,
            "is_nullable": is_nullable,
            "desc": desc,
        })
    
    # 判断基类类型
    base_type = "Fragment"
    if "Activity" in class_name:
        base_type = "Activity"
    elif "Fragment" in class_name:
        base_type = "Fragment"
    
    return {
        "class_name": class_name,
        "router_path": router_path,
        "base_type": base_type,
        "params": final_params,
    }

def generate_kt(info):
    """生成 Kotlin 测试文件"""
    class_name = info["class_name"]
    router_path = info["router_path"]
    base_type = info["base_type"]
    params = info["params"]
    
    imports = ["import launcher.Router"]
    if base_type == "Activity":
        imports.append("import android.app.Activity")
        extends = "Activity()"
    else:
        imports.append("import androidx.fragment.app.Fragment")
        extends = "Fragment()"
    if params:
        imports.append("import launcher.Boom")
    imports.sort()
    
    lines = [f"package {OUTPUT_PKG}", ""]
    for imp in imports:
        lines.append(imp)
    lines.append("")
    lines.append(f'@Router(routerPath = "{router_path}")')
    
    if not params:
        lines.append(f"class {class_name} : {extends}")
    else:
        lines.append(f"class {class_name} : {extends} {{")
        lines.append("")
        for p in params:
            parts = [f'index = {p["index"]}']
            if p["key"]:
                parts.append(f'key = "{p["key"]}"')
            if p["is_optional"]:
                parts.append("isOptional = true")
            parts.append(f'desc = "{p["desc"]}"')
            lines.append(f'    @Boom({", ".join(parts)})')
            nullable = p["is_nullable"] or p["is_optional"]
            type_str = "String?" if nullable else "String"
            default = " = null" if nullable else ' = ""'
            lines.append(f"    var {p['name']}: {type_str}{default}")
            lines.append("")
        lines.append("}")
    
    return "\n".join(lines)

def main():
    kapt_files = sorted([f for f in os.listdir(kapt_dir) if f.endswith("_XXXxxx.java")])
    
    # 先找出有差异的文件
    ksp_dir = "app/build/generated/ksp/debug/java/com/joker/event/router"
    diff_files = []
    for f in kapt_files:
        ksp_path = os.path.join(ksp_dir, f)
        kapt_path = os.path.join(kapt_dir, f)
        if not os.path.exists(ksp_path):
            diff_files.append(f)
            continue
        if open(kapt_path).read() != open(ksp_path).read():
            diff_files.append(f)
    
    print(f"Found {len(diff_files)} files with differences, regenerating...")
    
    fixed = 0
    errors = []
    for f in diff_files:
        kapt_path = os.path.join(kapt_dir, f)
        info = parse_kapt(kapt_path)
        if not info:
            errors.append(f"Failed to parse: {f}")
            continue
        
        kt_content = generate_kt(info)
        test_path = os.path.join(test_dir, info["class_name"] + ".kt")
        open(test_path, "w").write(kt_content)
        fixed += 1
    
    print(f"Regenerated {fixed} test files")
    if errors:
        print(f"Errors: {errors}")

if __name__ == "__main__":
    main()
