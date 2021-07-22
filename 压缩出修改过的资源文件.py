    
import os
from os.path import *
from zipfile import *

root = split(realpath(__file__))[0]
os.system('del archive.zip')
package = ZipFile(join(root, "archive.zip"),
 "w"
 , ZIP_LZMA
 #, ZIP_DEFLATED
 , compresslevel=9)
 
f = open('修改过的文件', 'r')

for filename in f:
	filename = filename.strip()
	pathname = join(root, filename)
	if len(filename)>0 and isfile(pathname):
		package.write(pathname, filename)
		print(filename)
		
package.close()
