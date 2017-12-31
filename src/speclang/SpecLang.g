grammar SpecLang;
import RecLang; 
 
exp returns [Exp ast]: 
	va=varexp { $ast = $va.ast; }
	| num=numexp { $ast = $num.ast; }
	| str=strexp { $ast = $str.ast; }
	| bl=boolexp { $ast = $bl.ast; }
    	| add=addexp { $ast = $add.ast; }
    	| sub=subexp { $ast = $sub.ast; }
    	| mul=multexp { $ast = $mul.ast; }
    	| div=divexp { $ast = $div.ast; }
    	| let=letexp { $ast = $let.ast; }
    	| lam=lambdaexp { $ast = $lam.ast; }
    	| call=callexp { $ast = $call.ast; }
    	| i=ifexp { $ast = $i.ast; }
    	| less=lessexp { $ast = $less.ast; }
    	| eq=equalexp { $ast = $eq.ast; }
    	| gt=greaterexp { $ast = $gt.ast; }
    	| car=carexp { $ast = $car.ast; }
    	| cdr=cdrexp { $ast = $cdr.ast; }
    	| cons=consexp { $ast = $cons.ast; }
    	| list=listexp { $ast = $list.ast; }
    	| nl=nullexp { $ast = $nl.ast; }
    	| lrec=letrecexp { $ast = $lrec.ast; }
        | np=isnumberexp { $ast = $np.ast; }
        | bp=isbooleanexp { $ast = $bp.ast; }
        | sp=isstringexp { $ast = $sp.ast; }
        | pp=isprocedureexp { $ast = $pp.ast; }
        | lp=islistexp { $ast = $lp.ast; }
        | pap=ispairexp { $ast = $pap.ast; }
        | up=isunitexp { $ast = $up.ast; }  
    	| ref=refexp { $ast = $ref.ast; }  
    	| deref=derefexp { $ast = $deref.ast; }
    	| assign=assignexp { $ast = $assign.ast; }
    	| free=freeexp { $ast = $free.ast; }
    	;
 
 refexp returns [RefExp ast] :
        '(' Ref
            e=exp
        ')' { $ast = new RefExp($e.ast); }
        ;

 derefexp returns [DerefExp ast] :
        '(' Deref
            e=exp
        ')' { $ast = new DerefExp($e.ast); }
        ;

 assignexp returns [AssignExp ast] :
        '(' Assign
            e1=exp
            e2=exp
        ')' { $ast = new AssignExp($e1.ast, $e2.ast); }
        ;

 freeexp returns [FreeExp ast] :
        '(' Free
            e=exp
         ')' { $ast = new FreeExp($e.ast); }
        ;

 /* Predicates for each type of value */ 
  
 isnumberexp returns [IsNumberExp ast] :
        '(' 'number?'
            e=exp
        ')' { $ast = new IsNumberExp($e.ast); }
        ;

 isbooleanexp returns [IsBooleanExp ast] :
        '(' 'boolean?'
            e=exp
        ')' { $ast = new IsBooleanExp($e.ast); }
        ;

 isstringexp returns [IsStringExp ast] :
        '(' 'string?'
            e=exp
        ')' { $ast = new IsStringExp($e.ast); }
        ;

 islistexp returns [IsListExp ast] :
        '(' 'list?'
            e=exp
        ')' { $ast = new IsListExp($e.ast); }
        ;

 ispairexp returns [IsPairExp ast] :
        '(' 'pair?'
            e=exp
        ')' { $ast = new IsPairExp($e.ast); }
        ;

 isunitexp returns [IsUnitExp ast] :
        '(' 'unit?'
            e=exp
        ')' { $ast = new IsUnitExp($e.ast); }
        ;

 isprocedureexp returns [IsProcedureExp ast] :
        '(' 'procedure?'
            e=exp
        ')' { $ast = new IsProcedureExp($e.ast); }
        ;

 isnullexp returns [IsNullExp ast] :
        '(' 'null?'
            e=exp
        ')' { $ast = new IsNullExp($e.ast); }
        ;
        
        
