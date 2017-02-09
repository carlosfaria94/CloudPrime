# CloudPrime

This is a implementation of an elastic
cluster of web servers that receive a post request with
a single parameter, a semiprime that is going to be factorized
and replied with the request back to whoever made the
request.

Our main goals were to make sure that the workers were
not overwhelmed with requests, therefore a good scheduling
and scaling algorithm were necessary to ensure that and
provide a reasonable client experience. Our other main goal
was not to lose any request, therefore making the cluster
fault-tolerant.

More detailed information in [Project Report](https://github.com/carlosfaria94/CloudPrime/blob/master/report.pdf).


## WebServer execution:

<code>export _JAVA_OPTIONS="-XX:-UseSplitVerifier"</code>

<code>bash compile.sh</code>

## LoadBalancer execution:

<code>javac *.java & java LoadBalancer</code>

Do a request with this structure:

<code>http://Load_Balancer_Public_IP:8000/f.html/n?=Intended_Number</code>


## Contributions

NOTE: This project is a culmination of work of the following members:

- Carlos Faria <carlosfigueira@tecnico.ulisboa.pt>
- Sérgio Mendes <sergiosmendes@tecnico.ulisboa.pt>
- Diogo Abreu <diogo.perestrelo.abreu@tecnico.ulisboa.pt>
