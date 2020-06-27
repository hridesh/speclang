(define length : (List<num> -> num)
	(lambda (l : List<num> 
			| #t -> (>= result 0))
		(if (null? l) 0
			(+ 1 (length (cdr l)))
		)
	)
)

(length (list : num  1 2 8))