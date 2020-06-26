(define f : (num -> num)
	(lambda 
		(x:num | (> x 0) -> (> result 0)) 
		(/ 342 x)
	)
)

(f 0) //Precondition violation here.