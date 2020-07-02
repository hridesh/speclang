// This example contains a specification that has side-effects.
// due to the use of function sx defined in let expression.
(let 
	((sx: (Ref num -> num) (lambda (x: Ref num) (set! x 0))))
	 (let 
	 	((f: (Ref num -> num)
	 		(lambda 
	 			(x: Ref num 
	 				| (> (deref x) (sx x)) -> (> result 7)) 
	 				(+ 3 (+ 4 (deref x)))
	 			) 
	 		)
	 	)
	 	(f (ref: num 2))
	 )
)