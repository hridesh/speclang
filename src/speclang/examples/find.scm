// Problem: specify find
(define find : (List<string> num -> string)
	(lambda (l : List<string> index : num 
			| #t -> #t)
		(if (null? l) "error"
			(if (= 0 index) (car l)
			    (find (cdr l) (- index 1))
			)
		)
	)
)

(find (list : string  "spec" "are" "important") 2)