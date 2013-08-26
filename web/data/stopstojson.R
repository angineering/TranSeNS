#! /usr/bin/env Rscript
library("rjson", lib.loc="R-packages")

y <- read.csv("stopdata.csv", header=FALSE)
i = 1
counter = 1
sum = 0
result <- list()
acc <- y[,2]
while(i <= length(acc)){
  sum = sum + 1
  stop <- list(weight=y[,2][i], lat=y[,3][i], lon=y[,4][i])
  result[[counter]] <- stop
  counter <- counter+1
  i = i + 1
}
result = list(stopData=result)
sum
write(toJSON(result), "stopdata.json")
