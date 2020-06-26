(define div342by : (num -> num)
	(lambda 
		(x:num | (> x 0) -> (> result 0)) 
		(/ 342 x)
	)
)

(div342by 0) //Precondition violation here.