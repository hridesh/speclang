(define or : (bool bool -> bool)
	(lambda (b1 : bool b2: bool)
		(if b1 #t b2)
	)
)

(define and : (bool bool -> bool)
	(lambda (b1 : bool b2: bool)
		(if b1 
			(if b2 #t #f) 
			#f
		)
	)
)

(define length : (List<num> -> num)
	(lambda (l : List<num> 
			| #t -> (>= result 0))
		(if (null? l) 0
			(+ 1 (length (cdr l)))
		)
	)
)

(define contains : (List<num> num -> bool)
	(lambda (l : List<num> n:num)
		(if (null? l) #f
			(if (= (car l) n) #t
				(contains (cdr l) n)
			)
		)
	)
)

(define forall : (List<num> (num -> bool) -> bool)
	(lambda (l : List<num> f: (num->bool))
		(if (null? l) #t
			(if (f (car l)) (forall (cdr l) f)
				#f
			)
		)
	)
)

(define post : (List<num> List<num> -> (num -> bool))
    (lambda (lst1 : List<num> lst2 : List<num>)
    	(lambda (n : num)
    		(or (contains lst1 n) (contains lst2 n))
    	)
    )
 )

(define append : (List<num> List<num> -> List<num>)
	(lambda (lst1: List<num> lst2: List<num> 
		| #t -> 
			(and 
				(= (length result) (+ (length lst1) (length lst2)))
				(forall result (post lst1 lst2))
			))
		(if (null? lst1) lst2
			(if (null? lst2) lst1
				(cons (car lst1) (append (cdr lst1) lst2))
			)
		)		
	)
)

(append (list: num 3) (list: num 4 2))