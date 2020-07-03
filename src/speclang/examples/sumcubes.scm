// Problem: specify sumcubes
(define sumcubes : (num -> num)
	(lambda (n : num | (>= n 0) -> (= result (/ (* n (+ n 1)) 2)))
		(if (= 0 n) n
			(+ (* n n n) (sumcubes (- n 1)))
		)		
	)
)

(sumcubes 3)