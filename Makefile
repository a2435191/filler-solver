.PHONY: run clean profile

SOURCES := $(wildcard *.java)
BUILD := build
JAVA := java -ea -cp $(BUILD)

$(BUILD)/Filler.class: $(SOURCES)
	javac -d $(BUILD) $(SOURCES)

run: $(BUILD)/Filler.class
	$(JAVA) Filler

clean:
	rm -rf $(BUILD)

profile: $(BUILD)/Filler.class
	time $(JAVA)\
		-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints\
    	-agentpath:/opt/homebrew/lib/libasyncProfiler.dylib=start,event=cpu,file=flamegraph.html\
    	Filler
