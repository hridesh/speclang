(define not : (bool -> bool) 
	(lambda (b : bool) 
		(if b #f #t)
	)
)

(define length : (List<num> -> num)
	(lambda (l : List<num> 
			| (null? l) -> (= result 0) 
				|| (not (null? l)) -> (= result (+ 1 (length (cdr l)))))
		(if (null? l) 0
			(+ 1 (length (cdr l)))
		)
	)
)

(length (list : num  1 2 8))