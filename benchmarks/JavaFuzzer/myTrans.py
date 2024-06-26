import os
import shutil

# 设置源文件夹和目标文件夹的路径
source_folder = 'tests6'  # 替换为你的源文件夹路径
destination_folder = 'tests66'  # 替换为你的目标文件夹路径

# 确保目标文件夹存在
os.makedirs(destination_folder, exist_ok=True)

# 遍历源文件夹中的所有子文件夹
for subdir in os.listdir(source_folder):
    subdir_path = os.path.join(source_folder, subdir)
    if os.path.isdir(subdir_path):
        java_file = os.path.join(subdir_path, 'Test.java')
        if os.path.isfile(java_file):
            # 构建新文件名和新类名
            new_filename = f'Test{subdir}.java'
            new_classname = f'Test{subdir}'

            # 读取原文件内容
            with open(java_file, 'r') as file:
                content = file.read()

            # 替换类名和实例创建代码
            content = content.replace('TestClassTest', f'Test{subdir}ClassTest')
            content = content.replace('class Test', f'class {new_classname}')
            content = content.replace('Test.', f'{new_classname}.')
            content = content.replace('Test _instance = new Test();', f'{new_classname} _instance = new {new_classname}();')
            content = content.replace('UserDefinedExceptionTest', f'UserDefinedExceptionTest{subdir}')

            # 将修改后的内容写入新文件
            new_file_path = os.path.join(destination_folder, new_filename)
            with open(new_file_path, 'w') as file:
                file.write(content)
