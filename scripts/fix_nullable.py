#!/usr/bin/env python3
"""修复 nullable 类型：从 kapt 签名判断参数是否 nullable，修正测试文件中的类型"""
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

print(f"{len(diff_files)} diff files")

for f in diff_files:
    kapt_content = open(os.path.join(kapt_dir, f)).read()
    class_name = f.replace("_XXXxxx.java", "")
    test_path = os.path.join(test_dir, class_name + ".kt")
    if not os.path.exists(test_path):
        continue

    schemes = re.findall(r'public static String getActionScheme\(([^)]*)\)', kapt_content)
    if not schemes:
        continue
    longest = max(schemes, key=len)
    longest_clean = re.sub(r'\s+', ' ', longest).strip()

    param_nullable = {}
    for part in re.split(r',\s*', longest_clean):
        part = part.strip()
        if not part:
            continue
        is_null = "@Nullable" in part
        nm = re.search(r'(\w+)\s*$', part)
        if nm:
            param_nullable[nm.group(1)] = is_null

    content = open(test_path).read()
    changed = False
    for param, is_null in param_nullable.items():
        if not is_null:
            # 非 nullable: String? = null -> String = ""
            pattern = rf'(var\s+{param}\s*:\s*)String\?\s*=\s*null'
            m = re.search(pattern, content)
            if m:
                content = content.replace(m.group(0), f'{m.group(1)}String = ""')
                changed = True

    if changed:
        open(test_path, "w").write(content)
        print(f"  Fixed: {class_name}")

print("Done")
