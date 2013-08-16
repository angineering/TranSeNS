#! /usr/bin/env Rscript
args <- commandArgs(TRUE)

y <- read.csv(args[1], header=FALSE)

size = length(y[,1])

# Checks if there is less than 6 seconds (0.10 min) between two timestamps
lt6sec<-function(j,i){
	if(j >= length(y[,1])){
	  return(FALSE)
  }
	t1 = y[,1][i]
	t2 = y[,1][j]
	tpassed = (t2-t1)/60000
	if(tpassed < 0.10){
	  return(TRUE)
	}
	return(FALSE)
}

inc3sec<-function(i){
  t1 = y[,1][i]
	inc = i+3
	t2 = y[,1][inc]
	if(inc >= size) return(size)
	inc = inc+1
	while(inc < size && (t2-t1)/60000 < 0.05){
		t2 = y[,1][inc]
		inc = inc+1
	}
	return(inc)
}

treshold = 1.5 
i = 1
count = 0
while(i < size){
	min_val = 0
	min_pos = size
	max_val = -10
	max_pos = 0
	j = i
	while(lt6sec(j,i)){
		# Check if min or max
		if(y[,2][j] < min_val){
			min_val = y[,2][j]
			min_pos = j
		}
		else if(y[,2][j] > max_val){
			max_val = y[,2][j]
			max_pos = j
		}
		j = j+1
	}
	delta = max_val - min_val
	if(delta >= treshold && min_pos < max_pos){
		bad_stop <- list(time=y[,1][max_pos], weight=delta, lat=y[,3][max_pos], lon=y[,4][max_pos])
		write.table(bad_stop, "stopdata.csv", append=TRUE, sep=",", row.names=FALSE, col.names=FALSE)
		count = count + 1
	}
	i = inc3sec(i)
}
count
