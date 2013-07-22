import httplib
import json
import ast
import math
import os
import sys
import argparse
import xlwt
import xlrd
import time
from xlwt import *

temp=0
chk1='false'
col=4
chkinit='true'
command = "curl -s http://127.0.0.1:8080/wm/topology/links/json"
result = os.popen(command).read()
parsedSwitch = json.loads(result)
style = XFStyle()
style.num_format_str = '0.00'
try:
   with open('example.xls'): pass
except IOError:
   chkinit='false'
while (temp == 0):
	if chkinit=='false':
		valbefore=range(250)
		g = 0
		while g<249:
			valbefore[g]=0
			g=g+1
	wb = xlwt.Workbook()
	ws = wb.add_sheet('A Test Sheet')
	y=0
	i=0
	if chkinit=='true':
		book = xlrd.open_workbook("example.xls")
		sh = book.sheet_by_index(0)
	for i in range(len(parsedSwitch)):
		if chkinit=='true':
			col=sh.row_len(i)
		if chkinit=='false':
			ap1Dpid = parsedSwitch[i]['src-switch']
			ap1Port = parsedSwitch[i]['src-port']
			ap2Dpid = parsedSwitch[i]['dst-switch']
			ap2Port = parsedSwitch[i]['dst-port']
		else:
			ap1Dpid = sh.cell_value(rowx=i, colx=0)
			ap1Port = sh.cell_value(rowx=i, colx=1)
			ap2Dpid = sh.cell_value(rowx=i, colx=2)
			ap2Port = sh.cell_value(rowx=i, colx=3)
		ws.write(i,0, ap1Dpid)
		ws.write(i,1, ap1Port)
		ws.write(i,2, ap2Dpid)
		ws.write(i,3, ap2Port)
		command = "curl -s http://127.0.0.1:8080/wm/core/switch/%s/port/json" % ap1Dpid
		result = os.popen(command).read()
		parsedResult = json.loads(result)
		for key, value in parsedResult.iteritems():
			value=value
		for ke in value:   
			for k, v in ke.iteritems():
				if k=='portNumber':
					if v==ap1Port:
						chk1='true'
				if k=='transmitBytes':
					chiave=k
					val=v
			if chk1=='true':
				ws.write(i,col, float((float(val)-float(valbefore[i]))*8/10000000), style)
				valbefore[i]=val
				if chkinit=='true':
					x=4
					while x < col:
						ws.write(i,x, float(sh.cell_value(rowx=i, colx=x)), style)
						x=x+1
			chk1='false'
	if chkinit=='true':		
		book.release_resources()
	wb.save('example.xls')
	print "Updated!"
	chkinit='true'
	time.sleep(7);
	
	
	
	
	
	
	
	
	