WebServer:
Ir para a pasta webserver/Projeto_CNV/src
Em primeiro lugar é necessário fazer: export _JAVA_OPTIONS="-XX:-UseSplitVerifier"
Depois é preciso fazer o export do classpath para a biblioteca da Amazon

Para ser mais rápido testar nós criamos um script que compila e corre a tool e o server.
Claro que também poderá ser testado colocando em /etc/rc.local e fizemos testes assim mas por enquanto, 
como há alterações constantes no código, achamos mais conveniente utilizar este script.
Para correr basta fazer "bash compile.sh" que faz as seguintes operações por ordem:
- Compila a nossa tool de instrumentação (InstTool.java)
- Compila o IntFactorization.java
- Compila o WebServer.java
- Corre a nossa tool de instrumentação no IntFactorization
- Corre o WebServer

LoadBalancer:
export do classpath para a biblioteca da Amazon
Ir para a pasta loadbalancer\src
javac *.java para compilar
java LoadBalancer para correr o LoadBalancer

Para fazer pedidos de fatorização:
http://IPPublicoLoadBalancer:8000/f.html/n?=NumeroPretendido