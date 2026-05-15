#!/usr/bin/env python3
"""从 kapt 生成文件中提取正确的 routerPath 和 key 值，修正 app 测试文件"""
import os, re

kapt_dir = "source/router/kapt"
test_dir = "app/src/main/java/com/joker/event/router"

kapt_files = [f for f in os.listdir(kapt_dir) if f.endswith("_XXXxxx.java")]
fixed = 0
errors = []

def camel_to_upper(name):
    return re.sub(r'([A-Z])', r'_\1', name).upper().lstrip('_') + '_INTENT_KEY'

for kapt_file in sorted(kapt_files):
    class_name = kapt_file.replace("_XXXxxx.java", "")
    test_file = os.path.join(test_dir, class_name + ".kt")
    if not os.path.exists(test_file):
        errors.append(f"Missing: {class_name}.kt")
        continue

    kapt_content = open(os.path.join(kapt_dir, kapt_file)).read()
    test_content = open(test_file).read()
    original = test_content

    # 1. 提取 ROUTER_ACTION
    m = re.search(r'ROUTER_ACTION\s*=\s*"([^"]*)";', kapt_content)
    if not m:
        errors.append(f"No ROUTER_ACTION: {kapt_file}")
        continue
    correct_path = m.group(1)

    # 2. 修正 routerPath
    old = re.search(r'@Router\(routerPath\s*=\s*"([^"]*)"\)', test_content)
    if old and old.group(1) != correct_path:
        test_content = test_content.replace(
            f'@Router(routerPath = "{old.group(1)}")',
            f'@Router(routerPath = "{correct_path}")'
        )

    # 3. 提取 KEY 常量
    key_map = {}
    for km in re.finditer(r'public static final String (\w+_INTENT_KEY)\s*=\s*"([^"]*)";', kapt_content):
        key_map[km.group(1)] = km.group(2)

    # 4. 修正每个 @Boom 的 key
    boom_pattern = re.compile(r'(@Boom\([^)]*\))\s*\n(\s*var\s+(\w+))')
    for bm in boom_pattern.finditer(test_content):
        boom_anno = bm.group(1)
        var_name = bm.group(3)
        field_const = camel_to_upper(var_name)

        if field_const not in key_map:
            continue
        correct_key = key_map[field_const]

        key_match = re.search(r'key\s*=\s*"([^"]*)"', boom_anno)
        current_key = key_match.group(1) if key_match else ""

        if current_key != correct_key:
            if key_match:
                new_anno = boom_anno.replace(f'key = "{current_key}"', f'key = "{correct_key}"')
            else:
                new_anno = re.sub(r'(index\s*=\s*\d+)', rf'\1, key = "{correct_key}"', boom_anno)
            test_content = test_content.replace(boom_anno, new_anno)

    if test_content != original:
        open(test_file, "w").write(test_content)
        fixed += 1

print(f"Fixed {fixed}/{len(kapt_files)} files")
if errors:
    print(f"Errors: {len(errors)}")
    for e in errors[:10]:
        print(f"  {e}")
