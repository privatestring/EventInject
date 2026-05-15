#!/usr/bin/env python3
"""
从 source/router/code 提取 @Router 注解信息，生成精简测试文件到 app router 包。
对于常量引用的 key/routerPath，直接用常量名作为字符串值（不搜索外部项目）。
"""
import os
import re
import sys

SOURCE_DIR = "source/router/code"
OUTPUT_DIR = "app/src/main/java/com/joker/event/router"
OUTPUT_PKG = "com.joker.event.router"

# 预编译正则
RE_PACKAGE = re.compile(r'^package\s+([\w.]+)', re.MULTILINE)
RE_ROUTER = re.compile(r'@Router\s*\(\s*routerPath\s*=\s*(.+?)(?:\s*,\s*cls\s*=.+?)?\s*,?\s*\)', re.DOTALL)
RE_BOOM_KOTLIN = re.compile(
    r'@Boom\s*\(([^)]*)\)\s*\n\s*(?:var|val)\s+(\w+)\s*:\s*(\w+\??)',
    re.MULTILINE
)
RE_BOOM_JAVA = re.compile(
    r'@Boom\s*\(([^)]*)\)\s*\n\s*(?:public\s+|private\s+|protected\s+)?(\w+)\s+(\w+)\s*[;=]',
    re.MULTILINE
)
RE_CLASS_KOTLIN = re.compile(
    r'(?:open\s+|abstract\s+)?class\s+(\w+)\s*(?:<[^>]*>)?\s*(?::\s*([^{(]+))?',
    re.MULTILINE
)
RE_CLASS_JAVA = re.compile(
    r'(?:public\s+)?class\s+(\w+)\s*(?:<[^>]*>)?\s*extends\s+(\w+)',
    re.MULTILINE
)

def resolve_string_expr(expr):
    """解析字符串表达式，直接字符串返回值，常量引用返回常量名"""
    expr = expr.strip().rstrip(",").strip()
    if expr.startswith('"') and expr.endswith('"'):
        return expr[1:-1]
    # 常量引用如 ActionCollection.TARGET_CUSTOMER_DETAIL -> 用最后一段小写作为值
    parts = expr.split(".")
    return parts[-1].lower() if parts else expr

def parse_boom_params(params_str):
    """解析 @Boom 注解参数"""
    result = {"index": "0", "key": "", "isOptional": False, "desc": "", "useFieldKey": False}
    m = re.search(r'index\s*=\s*(\d+)', params_str)
    if m:
        result["index"] = m.group(1)
    if re.search(r'isOptional\s*=\s*true', params_str):
        result["isOptional"] = True
    m = re.search(r'desc\s*=\s*"([^"]*)"', params_str)
    if m:
        result["desc"] = m.group(1)
    # key - 字符串
    m = re.search(r'key\s*=\s*"([^"]*)"', params_str)
    if m:
        result["key"] = m.group(1)
    else:
        # key - 常量引用
        m = re.search(r'key\s*=\s*([\w.]+)', params_str)
        if m:
            result["key"] = resolve_string_expr(m.group(1))
    if re.search(r'useFieldKey\s*=\s*true', params_str):
        result["useFieldKey"] = True
    return result

def determine_base_class(content, is_java):
    """判断基类类型"""
    if is_java:
        m = RE_CLASS_JAVA.search(content)
        parent = (m.group(2) if m else "") or ""
    else:
        m = RE_CLASS_KOTLIN.search(content)
        parent = (m.group(2) if m else "") or ""
    parent_lower = parent.lower()
    if "activity" in parent_lower:
        return "Activity"
    elif "dialogfragment" in parent_lower or "bottomdialog" in parent_lower:
        return "DialogFragment"
    elif "fragment" in parent_lower:
        return "Fragment"
    elif "receiver" in parent_lower:
        return "BroadcastReceiver"
    else:
        # 用类名猜测
        class_name = (m.group(1) if m else "") or ""
        if "Activity" in class_name:
            return "Activity"
        elif "Fragment" in class_name:
            return "Fragment"
        return "Fragment"

def process_file(filename):
    """处理单个源文件"""
    filepath = os.path.join(SOURCE_DIR, filename)
    content = open(filepath, encoding='utf-8').read()
    is_java = filename.endswith('.java')

    pkg_match = RE_PACKAGE.search(content)
    if not pkg_match:
        return None

    router_match = RE_ROUTER.search(content)
    if not router_match:
        return None
    router_path = resolve_string_expr(router_match.group(1).strip())

    if is_java:
        cls_match = RE_CLASS_JAVA.search(content)
    else:
        cls_match = RE_CLASS_KOTLIN.search(content)
    if not cls_match:
        return None
    class_name = cls_match.group(1)

    base_type = determine_base_class(content, is_java)

    booms = []
    if is_java:
        for m in RE_BOOM_JAVA.finditer(content):
            params = parse_boom_params(m.group(1))
            params["name"] = m.group(3)
            params["nullable"] = True  # Java String 默认 nullable
            booms.append(params)
    else:
        for m in RE_BOOM_KOTLIN.finditer(content):
            params = parse_boom_params(m.group(1))
            params["name"] = m.group(2)
            params["nullable"] = m.group(3).endswith("?")
            booms.append(params)

    booms.sort(key=lambda x: int(x.get("index", "0")))
    return generate_test_file(class_name, router_path, base_type, booms)

def generate_test_file(class_name, router_path, base_type, booms):
    """生成 Kotlin 测试文件"""
    imports = ["import launcher.Router"]
    if base_type == "Activity":
        imports.append("import android.app.Activity")
        extends = "Activity()"
    elif base_type == "DialogFragment":
        imports.append("import androidx.fragment.app.DialogFragment")
        extends = "DialogFragment()"
    elif base_type == "Fragment":
        imports.append("import androidx.fragment.app.Fragment")
        extends = "Fragment()"
    else:
        imports.append("import android.content.BroadcastReceiver")
        extends = "BroadcastReceiver()"

    if booms:
        imports.append("import launcher.Boom")
    imports.sort()

    lines = [f"package {OUTPUT_PKG}", ""]
    for imp in imports:
        lines.append(imp)
    lines.append("")
    lines.append(f'@Router(routerPath = "{router_path}")')

    if not booms:
        lines.append(f"class {class_name} : {extends}")
    else:
        lines.append(f"class {class_name} : {extends} {{")
        lines.append("")
        for boom in booms:
            parts = [f'index = {boom["index"]}']
            if boom.get("key"):
                parts.append(f'key = "{boom["key"]}"')
            if boom.get("isOptional"):
                parts.append("isOptional = true")
            if boom.get("useFieldKey"):
                parts.append("useFieldKey = true")
            parts.append(f'desc = "{boom.get("desc", "")}"')
            lines.append(f'    @Boom({", ".join(parts)})')
            nullable = boom.get("isOptional", False) or boom.get("nullable", False)
            type_str = "String?" if nullable else "String"
            default = " = null" if nullable else ' = ""'
            lines.append(f"    var {boom['name']}: {type_str}{default}")
            lines.append("")
        lines.append("}")

    return (class_name, "\n".join(lines))

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    files = sorted(os.listdir(SOURCE_DIR))
    total = len(files)
    print(f"[1/3] Scanning {total} source files...")

    results = []
    for i, f in enumerate(files):
        result = process_file(f)
        if result:
            results.append(result)
        if (i + 1) % 20 == 0 or (i + 1) == total:
            print(f"[2/3] Parsed {i+1}/{total} files, found {len(results)} with @Router")

    print(f"[3/3] Writing {len(results)} test files...")
    for class_name, content in results:
        output_path = os.path.join(OUTPUT_DIR, f"{class_name}.kt")
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(content)

    print(f"Done! Generated {len(results)} files in {OUTPUT_DIR}")

if __name__ == "__main__":
    main()
