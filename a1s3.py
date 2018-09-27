# -*- coding: utf-8 -*-
file = open("chr22.fa")
first = file.readline()
file_context = file.read()
k = 101
count = 0
tmp_ret = ""
for i in range(len(file_context) - k):
    substr = file_context[i : i + k - 1]
    tmp_cg = 0
    for j in substr:
        if j == 'C' or j == 'c' or j == 'g' or j == 'G':
            tmp_cg += 1
    tmp_cg_content = tmp_cg / len(substr)
    if tmp_cg_content > 0.7:
        count += 1
        tmp_ret += '1'
    else:
        tmp_ret += '0'   
print("High-CG Content result: ", count)
f = open("out.txt", "w+")
print(first, file = f)
for i in range(len(tmp_ret)):
    if i < len(tmp_ret) - 1:
        if tmp_ret[i] == '0':
            print(file_context[i], file = f)
        else:
            if file_context[i] == 'A' or file_context[i] == 'T' or file_context[i] == 'C' or file_context[i] == 'G' or file_context[i] == 'N':
                print('N', file = f)
            else:
                print('n', file = f)
    else:
        if tmp_ret[i] == '0':
            print(file_context[i: len(file_context)], file = f)
        else:
            for j in file_context[i: len(file_context)]:
                if j == 'A' or j == 'T' or j == 'C' or j == 'G' or j == 'N':
                    print('N', file = f)
                else:
                    print('n', file = f)
print("File output successfully!")