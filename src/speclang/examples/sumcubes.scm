// Problem: specify sumcubes
(define sumcubes : (num -> num)
	(lambda (n : num | #t -> #t )
		(if (= 0 n) n
			(+ (* n n n) (sumcubes (- n 1)))
		)		
	)
)

(sumcubes 3)