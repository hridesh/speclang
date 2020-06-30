(define natsum : (num -> num)
	(lambda (n : num | (>= n 0) -> (= result (/ (* n (+ n 1)) 2)))
		(if (= 0 n) n
			(+ n (natsum (- n 1)))
		)		
	)
)

(natsum 3)