# Proyecto del Primer Parcial

En este proyecto, implementaremos un sistema distribuido, que permitirá leer archivos de
varias carpetas, de cada archivo obtener la frecuencia de aparición de las palabras y luego
utilizar una cola de trabajo para enviar esa información hacia el programa consumidor que
agregará la información.

## Comenzando 🚀

_Estas instrucciones te permitirán obtener una copia del proyecto en funcionamiento en tu máquina local para propósitos de desarrollo y pruebas._

```
$ sudo apt install git
$ git clone https://github.com/Pvidal-1/uees-sd-2022-repo02.git
```

### Pre-requisitos 📋

_Que cosas necesitas para instalar el software y como instalarlas_

Instalar JDK, Maven y curl

```
$ sudo apt update
$ sudo apt install default-jdk 
$ sudo apt install maven
$ sudo apt install curl
```

Instalar Erlang

```
$ sudo apt update
$ sudo apt install software-properties-common apt-transport-https
$ wget -O- https://packages.erlang-solutions.com/ubuntu/erlang_solutions.asc | sudo apt-key add -
$ echo "deb https://packages.erlang-solutions.com/ubuntu focal contrib" | sudo tee /etc/apt/sources.list.d/rabbitmq.list
$ sudo apt update
$ sudo apt install erlang
```
Agregar RabbitMQ al repositorio

```
$ curl -s https://packagecloud.io/install/repositories/rabbitmq/rabbitmq-server/script.deb.sh | sudo bash
$ sudo apt update
$ sudo apt install rabbitmq-server
```

## Confirmando instalacion y funcionamiento de RabbitMQ ⚙️

_Para comprobar el estado de RabbitMQ_

```
$ systemctl status  rabbitmq-server.service
```

_Para comprobar si RabbitMQ esta activado_

```
$ systemctl is-enabled rabbitmq-server.service 
```

_Si no lo esta_

```
$ sudo systemctl enable rabbitmq-server
```
## Despliegue 📦

_Desde el directorio del proyecto, para compilar los .java y crear el ejecutable .jar_

```
$ mvn clean install
```

_Para ejecutar el .jar en el directorio del proyecto_

```
$ java -jar target/Executable.jar
```

## Pruebas del queue ⚙️

_Entrar con permisos_

```
$ sudo su
```

_Para comprobar que se agregó el mensaje al queue RabbitMQ_

```
$ rabbitmqctl list_queues
```

_Para eliminar los mensajes de los queues_
```
$ rabbitmqctl purge_queue myRabbitQueue
```


## Construido con 🛠️

* [RabbitMQ](https://www.rabbitmq.com/) - Message Broker de Codigo Abierto
* [Maven](https://maven.apache.org/) - Manejador de dependencias

## Autores ✒️

* **Nicolás Cevallos** - [nicolascevallosbUEES](https://github.com/nicolascevallosbuees/)
* **Pedro Vidal** - [Pvidal-1](https://github.com/Pvidal-1/)
