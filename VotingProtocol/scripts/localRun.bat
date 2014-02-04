cd ..
cp -r bin/code .

start java code.server.ServerDriver 1
start java code.server.ServerDriver 2
start java code.server.ServerDriver 3
start java code.server.ServerDriver 4
start java code.server.ServerDriver 5
start java code.server.ServerDriver 6
start java code.server.ServerDriver 7
sleep 1
start java code.client.ClientDriver 1
start java code.client.ClientDriver 2
start java code.client.ClientDriver 3
start java code.client.ClientDriver 4
start java code.client.ClientDriver 5
cd scripts