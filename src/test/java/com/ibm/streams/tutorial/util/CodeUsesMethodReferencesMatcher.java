package com.ibm.streams.tutorial.util;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public final class CodeUsesMethodReferencesMatcher extends TypeSafeDiagnosingMatcher<Class<?>> {

    private final String methodName;

    private CodeUsesMethodReferencesMatcher(String methodName) {
        this.methodName = methodName;
    }

    public static CodeUsesMethodReferencesMatcher usesMethodReferences(String methodName) {
        return new CodeUsesMethodReferencesMatcher(methodName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a source file using a method reference to invoke ").appendValue(methodName);
    }


    @Override
    protected boolean matchesSafely(Class<?> clazz, Description mismatchDescription) {
        try {
            Optional<String> sourceFileContent = getSourceContent(clazz);
            return sourceFileContent.map(c -> usesMethodReference(c, mismatchDescription)).orElseGet(() -> {
                mismatchDescription.appendText("could not read source file to discover if you used method references.");
                return false;
            });
        } catch (IOException e) {
            mismatchDescription.appendText("could not read source file to discover if you used method references.");
            mismatchDescription.appendValue(e);
            return false;
        }
    }

    private boolean usesMethodReference(String sourceCode, Description mismatchDescription) {
        if (sourceCode.contains("::"+methodName)) {
            return true;
        } else {
            mismatchDescription.appendText("source code did not use a method reference to invoke " + methodName + ". ");
            context(sourceCode, methodName, mismatchDescription);
            return false;
        }
    }

    private void context(String sourceCode, String methodName, Description mismatchDescription) {
        if (!sourceCode.contains(methodName)) {
            mismatchDescription.appendText("You did not appear to invoke the method at all.");
        } else {
            String[] lines = sourceCode.split("\\n");
            mismatchDescription.appendText("Actual invocations: ");
            mismatchDescription.appendValueList("[", ",", "]",
                    Arrays.stream(lines).filter(l -> l.contains(methodName)).map(String::trim).collect(toList()));
        }
    }

    private Optional<String> getSourceContent(Class<?> clazz) throws IOException {
        String sourceFileName = getSourceFileName(clazz);
        Optional<File> sourceFile = findPathTo(sourceFileName);

        return sourceFile.map(this::toContent);
    }

    private Optional<File> findPathTo(String sourceFileName) throws IOException {
        File cwd = new File(".");
        File rootOfProject = findRootOfProject(cwd);
        return findSourceFile(rootOfProject, sourceFileName);
    }

    private String toContent(File file) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(file.toURI()));
            return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not read Java source file.", e);
        }
    }

    private Optional<File> findSourceFile(File rootOfProject, String sourceFileName) throws IOException {
        Path startingDir = Paths.get(rootOfProject.toURI());
        return Files.find(startingDir, 15, (path, attrs) -> path.endsWith(sourceFileName))
                    .map(p -> new File(p.toUri()))
                    .findFirst();
    }

    private File findRootOfProject(File cwd) {
        File[] pomFiles = cwd.listFiles((file, name) -> { return name.equals("build.gradle"); });
        if (pomFiles != null && pomFiles.length == 1) {
            return cwd;
        } else if (cwd.getParentFile() == null) {
            throw new RuntimeException("Couldn't find directory containing build.gradle. Last looked in: " + cwd.getAbsolutePath());
        } else {
            return findRootOfProject(cwd.getParentFile());
        }
    }

    private String getSourceFileName(Class<?> clazz) throws IOException {
        String resourceName = clazz.getName().replace(".", "/").concat(".class");
        ClassReader reader = new ClassReader(clazz.getClassLoader().getResourceAsStream(resourceName));
        SourceFileNameVisitor sourceFileNameVisitor = new SourceFileNameVisitor();
        reader.accept(sourceFileNameVisitor, 0);

        return sourceFileNameVisitor.getSourceFile();
    }


    private static final class SourceFileNameVisitor extends ClassVisitor {

        private String sourceFile = null;
        private boolean visitedYet = false;

        public SourceFileNameVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitSource(String source, String debug) {
            this.visitedYet = true;
            this.sourceFile = source;
            super.visitSource(source, debug);
        }

        public String getSourceFile() {
            if (!visitedYet) throw new IllegalStateException("Must visit a class before asking for source file");
            return this.sourceFile;
        }
    }

}
