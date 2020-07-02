// This example contains a contract that has side-effects.
 (let 
 	((f: (Ref num -> num)
 		(lambda 
 			(x: Ref num 
 				| (> (deref x) (set! x 0)) -> (> result 7)) 
 				(+ 3 (+ 4 (deref x)))
 			) 
 		)
 	)
 	(f (ref: num 2))
 )