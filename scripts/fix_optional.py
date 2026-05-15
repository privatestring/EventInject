#!/usr/bin/env python3
"""修复剩余差异：从 kapt 判断哪些参数是 optional，修正测试文件"""
import os, re

kapt_dir = "source/router/kapt"
ksp_dir = "app/build/generated/ksp/debug/java/com/joker/event/router"
test_dir = "app/src/main/java/com/joker/event/router"

kapt_files = sorted([f for f in os.listdir(kapt_dir) if f.endswith("_XXXxxx.java")])
diff_files = []
for f in kapt_files:
    ksp_path = os.path.join(ksp_dir, f)
    kapt_path = os.path.join(kapt_dir, f)
    if not os.path.exists(ksp_path) or open(kapt_path).read() != open(ksp_path).read():
        diff_files.append(f)

print(f"{len(diff_files)} diff files to fix")

for f in diff_files:
    kapt_content = open(os.path.join(kapt_dir, f)).read()
    class_name = f.replace("_XXXxxx.java", "")
    test_path = os.path.join(test_dir, class_name + ".kt")
    if not os.path.exists(test_path):
        print(f"  SKIP (missing): {class_name}")
        continue

    # 提取所有 getActionScheme 签名
    schemes = re.findall(r'public static String getActionScheme\(([^)]*)\)', kapt_content)
    has_no_param = any(s.strip() == "" for s in schemes)

    # 从最长签名提取所有参数名
    longest = max(schemes, key=len) if schemes else ""
    longest_clean = re.sub(r'\s+', ' ', longest).strip()
    # 提取 @Nullable 信息
    param_nullable = {}
    for part in re.split(r',\s*', longest_clean):
        part = part.strip()
        if not part:
            continue
        is_null = "@Nullable" in part
        nm = re.search(r'(\w+)\s*$', part)
        if nm:
            param_nullable[nm.group(1)] = is_null

    # 从最短签名确定必填参数
    if has_no_param:
        optional_params = set(param_nullable.keys())
    else:
        non_empty = [s for s in schemes if s.strip()]
        if non_empty:
            shortest = min(non_empty, key=len)
            shortest_clean = re.sub(r'\s+', ' ', shortest).strip()
            required = set()
            for part in re.split(r',\s*', shortest_clean):
                nm = re.search(r'(\w+)\s*$', part.strip())
                if nm:
                    required.add(nm.group(1))
            optional_params = set(param_nullable.keys()) - required
        else:
            optional_params = set()

    # 修正测试文件
    content = open(test_path).read()
    changed = False

    for param in optional_params:
        # 添加 isOptional
        pattern = rf'(@Boom\([^)]*\))\s*\n(\s*var\s+{param}\s*:)'
        m = re.search(pattern, content)
        if m:
            boom = m.group(1)
            if "isOptional" not in boom:
                new_boom = boom.replace("desc =", "isOptional = true, desc =")
                content = content.replace(boom, new_boom)
                changed = True

        # 修正类型为 String?
        var_pattern = rf'(var\s+{param}\s*:\s*)String(\s*=\s*"")'
        vm = re.search(var_pattern, content)
        if vm:
            content = content.replace(vm.group(0), f"{vm.group(1)}String? = null")
            changed = True

    if changed:
        open(test_path, "w").write(content)
        print(f"  Fixed: {class_name}")
    else:
        print(f"  No change needed: {class_name}")

print("Done")
