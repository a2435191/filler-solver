.PHONY: run clean profile

Filler.class: Filler.java
	javac Filler.java

run: Filler.class
	java -ea Filler

clean:
	rm *.class

profile: Filler.class
	time java -ea -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints\
    	-agentpath:/opt/homebrew/lib/libasyncProfiler.dylib=start,event=cpu,file=flamegraph.html\
    	Filler
