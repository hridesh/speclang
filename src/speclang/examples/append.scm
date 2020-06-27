(define length : (List<num> -> num)
	(lambda (l : List<num> 
			| #t -> (>= result 0))
		(if (null? l) 0
			(+ 1 (length (cdr l)))
		)
	)
)

(define append : (List<num> List<num> -> List<num>)
	(lambda (lst1: List<num> lst2: List<num> | #t -> (= (length result) (+ (length lst1) (length lst2))))
		(if (null? lst1) lst2
			(if (null? lst2) lst1
				(cons (car lst1) (append (cdr lst1) lst2))
			)
		)		
	)
)

(append (list: num 3) (list: num 4 2))