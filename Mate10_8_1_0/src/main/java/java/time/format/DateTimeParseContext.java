package java.time.format;

import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

final class DateTimeParseContext {
    private boolean caseSensitive = true;
    private ArrayList<Consumer<Chronology>> chronoListeners = null;
    private DateTimeFormatter formatter;
    private final ArrayList<Parsed> parsed = new ArrayList();
    private boolean strict = true;

    int setParsedField(java.time.temporal.TemporalField r1, long r2, int r4, int r5) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.time.format.DateTimeParseContext.setParsedField(java.time.temporal.TemporalField, long, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:31)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:296)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.time.format.DateTimeParseContext.setParsedField(java.time.temporal.TemporalField, long, int, int):int");
    }

    DateTimeParseContext(DateTimeFormatter formatter) {
        this.formatter = formatter;
        this.parsed.add(new Parsed());
    }

    DateTimeParseContext copy() {
        DateTimeParseContext newContext = new DateTimeParseContext(this.formatter);
        newContext.caseSensitive = this.caseSensitive;
        newContext.strict = this.strict;
        return newContext;
    }

    Locale getLocale() {
        return this.formatter.getLocale();
    }

    DecimalStyle getDecimalStyle() {
        return this.formatter.getDecimalStyle();
    }

    Chronology getEffectiveChronology() {
        Chronology chrono = currentParsed().chrono;
        if (chrono != null) {
            return chrono;
        }
        chrono = this.formatter.getChronology();
        if (chrono == null) {
            return IsoChronology.INSTANCE;
        }
        return chrono;
    }

    boolean isCaseSensitive() {
        return this.caseSensitive;
    }

    void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    boolean subSequenceEquals(CharSequence cs1, int offset1, CharSequence cs2, int offset2, int length) {
        if (offset1 + length > cs1.length() || offset2 + length > cs2.length()) {
            return false;
        }
        int i;
        if (isCaseSensitive()) {
            for (i = 0; i < length; i++) {
                if (cs1.charAt(offset1 + i) != cs2.charAt(offset2 + i)) {
                    return false;
                }
            }
        } else {
            for (i = 0; i < length; i++) {
                char ch1 = cs1.charAt(offset1 + i);
                char ch2 = cs2.charAt(offset2 + i);
                if (ch1 != ch2 && Character.toUpperCase(ch1) != Character.toUpperCase(ch2) && Character.toLowerCase(ch1) != Character.toLowerCase(ch2)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean charEquals(char ch1, char ch2) {
        if (!isCaseSensitive()) {
            return charEqualsIgnoreCase(ch1, ch2);
        }
        return ch1 == ch2;
    }

    static boolean charEqualsIgnoreCase(char c1, char c2) {
        if (c1 == c2 || Character.toUpperCase(c1) == Character.toUpperCase(c2) || Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
            return true;
        }
        return false;
    }

    boolean isStrict() {
        return this.strict;
    }

    void setStrict(boolean strict) {
        this.strict = strict;
    }

    void startOptional() {
        this.parsed.add(currentParsed().copy());
    }

    void endOptional(boolean successful) {
        if (successful) {
            this.parsed.remove(this.parsed.size() - 2);
        } else {
            this.parsed.remove(this.parsed.size() - 1);
        }
    }

    private Parsed currentParsed() {
        return (Parsed) this.parsed.get(this.parsed.size() - 1);
    }

    Parsed toUnresolved() {
        return currentParsed();
    }

    TemporalAccessor toResolved(ResolverStyle resolverStyle, Set<TemporalField> resolverFields) {
        Parsed parsed = currentParsed();
        parsed.chrono = getEffectiveChronology();
        parsed.zone = parsed.zone != null ? parsed.zone : this.formatter.getZone();
        return parsed.resolve(resolverStyle, resolverFields);
    }

    Long getParsed(TemporalField field) {
        return (Long) currentParsed().fieldValues.get(field);
    }

    void setParsed(Chronology chrono) {
        Objects.requireNonNull((Object) chrono, "chrono");
        currentParsed().chrono = chrono;
        if (this.chronoListeners != null && (this.chronoListeners.isEmpty() ^ 1) != 0) {
            Consumer[] listeners = (Consumer[]) this.chronoListeners.toArray(new Consumer[1]);
            this.chronoListeners.clear();
            for (Consumer<Chronology> l : listeners) {
                l.accept(chrono);
            }
        }
    }

    void addChronoChangedListener(Consumer<Chronology> listener) {
        if (this.chronoListeners == null) {
            this.chronoListeners = new ArrayList();
        }
        this.chronoListeners.add(listener);
    }

    void setParsed(ZoneId zone) {
        Objects.requireNonNull((Object) zone, "zone");
        currentParsed().zone = zone;
    }

    void setParsedLeapSecond() {
        currentParsed().leapSecond = true;
    }

    public String toString() {
        return currentParsed().toString();
    }
}
