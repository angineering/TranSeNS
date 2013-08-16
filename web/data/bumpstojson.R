#! /usr/bin/env Rscript
library("rjson", lib.loc="R-packages")

z <- read.csv("bumpdata.csv", header=FALSE)
summary(z)
i = 1 
counter = 1
sum = 0
result <- list()
acc <- z[,2]
while(i <= length(acc)){
  sum = sum + 1
  bump <- list(weight=z[,2][i], lat=z[,3][i], lon=z[,4][i])
  result[[counter]] <- bump
  counter <- counter+1
  i = i + 1
}
result = list(bumpData=result)
sum
write(toJSON(result), "bumpdata.json")
