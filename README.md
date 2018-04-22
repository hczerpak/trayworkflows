# trayworkflows
My solution to tray.io Backend Engineering Technical Test (2016)

### Running with Docker (build & run)
To build using docker use this:
```
docker build -t hc/trayio https://github.com/hczerpak/trayworkflows.git
```
and then to run:
```
docker run -p 9000:9000 hc/trayio
```

### Running in terminal (clone & run)
Assuming installed tools git, sbt, java etc:
```
git clone https://github.com/hczerpak/trayworkflows.git
cd trayworkflows
sbt run
```

### Comments from me:
- The assignment is quite large comparing to few other assignments I've done in my life for job interviews. This is the one which required the most of work (or time spent) from me.
- I have not implemented any performance measurements because it's already taken me 3 full days and I don't think tray.io intention was for anyone to spend more time than this. 
- This is first time I'm writing any Akka actors and using Akka http library.
- It's very time consuming to test my Dockerfile on vacation without good internet connection so I'm hoping it works.

Hubert Czerpak