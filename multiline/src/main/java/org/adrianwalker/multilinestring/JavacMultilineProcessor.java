package org.adrianwalker.multilinestring;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

@SupportedAnnotationTypes({"org.adrianwalker.multilinestring.Multiline"})
public final class JavacMultilineProcessor extends AbstractProcessor {

	private JavacElements elementUtils;
	private TreeMaker maker;
  
	@Override
	public void init(final ProcessingEnvironment procEnv) {
		super.init(procEnv);
		JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) procEnv;
		this.elementUtils = javacProcessingEnv.getElementUtils();
		this.maker = TreeMaker.instance(javacProcessingEnv.getContext());
	}

	@Override public SourceVersion getSupportedSourceVersion() {
               return SourceVersion.latest();
	}

	public static Pattern comments=Pattern.compile("((?<![:/])//.*)?(\r)?\n");

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		Set<? extends Element> fields = roundEnv.getElementsAnnotatedWith(Multiline.class);
		for (javax.lang.model.element.Element field : fields) {
			if(field.getKind() == ElementKind.FIELD){
				String docComment = elementUtils.getDocComment(field);
				if (null != docComment) {
					Multiline annotation = field.getAnnotation(Multiline.class);
					if(annotation.trim()){
						docComment=comments.matcher(docComment).replaceAll(" ");
						docComment=docComment.replaceAll("\\s+"," ");
						docComment=docComment.replaceAll(" ?([={}<>;,+\\-]) ?","$1");
					}
					
					JCVariableDecl fieldNode = (JCVariableDecl) elementUtils.getTree(field);
					TypeName typeName = TypeName.get(field.asType());
					String name = typeName.toString();
					
					if(name.equals(String.class.getName())) {
						fieldNode.init = maker.Literal(docComment);
					} else if(name.equals("byte[]")) {
						byte[] data = docComment.getBytes(StandardCharsets.UTF_8);
						JCTree.JCExpression[] dataExp = new JCTree.JCExpression[data.length];
						for (int i = 0; i < data.length; i++) {
							dataExp[i] = maker.Literal((int)data[i]);
						}
						fieldNode.init = maker.NewArray(maker.TypeIdent(TypeTag.BYTE), List.nil(), List.from(dataExp));
					}
				}
			}
		}
		
		return true;
	}
}
