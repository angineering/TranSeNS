#! /usr/bin/env Rscript
args <- commandArgs(TRUE)

z <- read.csv(args[1], header=FALSE)
summary(z)
i = 2
count = 0
acc <- z[,2]
while(i < length(acc)){
  delta = acc[i] - acc[i-1]
  if(delta >=1.5){
    count = count + 1
    i = i + 2 
    bump <- list(time=z[,1][i], weight=delta, lat=z[,3][i], lon=z[,4][i])
    write.table(bump, "bumpdata.csv", append=TRUE, sep=",", row.names=FALSE, col.names=FALSE)
  } else {
    i = i + 1
  }
}
count
