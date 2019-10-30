Multiline
=========

An implementation of multiline string literals in Java, using Javadoc comments. 

This project is originated from Adrian Walker's blog post ( <http://www.adrianwalker.org/2011/12/java-multiline-string.html> ).

## Usage
You can use multiline string literals with javadoc comments and '@Multiline' annotation.

For example,

	/**
	DELETE
 	FROM post
 	*/
	@Multiline static String deleteFromPost;

is equivalent to the following expression in Groovy.

	static String deleteFromPost = """
	DELETE
	FROM post
	"""

## Configuration
- [Maven project with Eclipse](https://github.com/benelog/multiline/wiki/Maven-project-with-Eclipse)
- [Non-Maven Java project with Eclipse](https://github.com/benelog/multiline/wiki/Non-Maven-Java-project-with-Eclipse)

## Tips
- [Create a template for Multiline-string in Eclipse](https://github.com/benelog/multiline/wiki/Create-a-template-for-Multiline-string-in-Eclipse)

## Release Notes
- [0.1.2](https://github.com/benelog/multiline/wiki/0.1.2)  (2015-09-08)
- [0.1.1](https://github.com/benelog/multiline/wiki/0.1.1)  (2012-01-28)
- 0.1.0 : the source code from [Adrian Walker's blog post](http://www.adrianwalker.org/2011/12/java-multiline-string.html)
