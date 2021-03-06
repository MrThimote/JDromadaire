package functions;

import java.math.BigDecimal;
import java.math.MathContext;

import ch.obermuhlner.math.big.BigDecimalMath;
import functions.base.array.MapFunction;
import functions.base.cast.ToBooleanFunction;
import functions.base.cast.ToNumberFunction;
import functions.base.cast.ToStringFunction;
import functions.files.FileNode;
import libs.LibLoader;
import main.EntryPoint;
import variables.NativeMethodNode;
import variables.VariableContext;

public class FunctionImporter {
	
	public static void ImportFunctions() {
		VariableContext cont = EntryPoint.globalContext;
		
		BigDecimalMath.acos(BigDecimal.ZERO, MathContext.DECIMAL64);
		
		cont.setValue("print", new PrintFunction(-2, -2));
		cont.setValue("help", new HelpFunction(-2,-2));
		cont.setValue("input", new InputFunction(-2,-2));
		cont.setValue("map", new MapFunction(-2,-2));
		cont.setValue("str", new ToStringFunction(-2,-2));
		cont.setValue("int", new ToNumberFunction(-2,-2, 0));
		cont.setValue("number", new ToNumberFunction(-2,-2, 1));
		cont.setValue("boolean", new ToBooleanFunction(-2,-2));
		cont.setValue("File", new FileNode(-2,-2));
		cont.setValue("range", new RangeFunction(-2,-2));
		cont.setValue("sum", new SumFunction(-2,-2));
		cont.setValue("chr", new ChrFunction(-2,-2));
		cont.setValue("ord", new OrdFunction(-2,-2));
		cont.setValue("type", new TypeFunction(-2,-2));

		// Math
		cont.setValue("round", new RoundFunction(-2,-2));
		
		LibLoader.registerModule("openns", "libs/lib_openns.jar");
		LibLoader.registerModule("math", "libs/math.jar");
		LibLoader.registerModule("time", "libs/lib_time.jar");
		LibLoader.registerModule("random", "libs/lib_random.jar");
		LibLoader.registerModule("opengl", "libs/lib_opengl.jar");
	}
	
}
