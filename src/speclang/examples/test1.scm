(define length : (List<num> -> num)
	(lambda (l : List<num>)
		(if (null? l) 0
			(+ 1 (length (cdr l)))
		)
	)
)

(define append : (List<num> List<num> -> List<num>)
	(lambda (lst1: List<num> lst2: List<num>)
		(if (null? lst1) lst2
			(if (null? lst2) lst1
				(cons (car lst1) (append (cdr lst1) lst2))
			)
		)		
	)
)
