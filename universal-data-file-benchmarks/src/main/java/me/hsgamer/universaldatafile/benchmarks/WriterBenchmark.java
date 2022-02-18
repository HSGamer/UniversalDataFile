package me.hsgamer.universaldatafile.benchmarks;

import me.hsgamer.universaldatafile.UniversalDataWriter;
import me.hsgamer.universaldatafile.objects.TestWriter;
import org.openjdk.jmh.annotations.*;

import java.io.Writer;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
public class WriterBenchmark {
    private UniversalDataWriter writer;
    @Param({"1", "10", "50", "1000"})
    private int limitQueue;
    @Param({"10", "100", "1000"})
    private int writerCount;

    @Setup
    public void setup() {
        writer = UniversalDataWriter.create()
                .setWriter(new Writer() {
                    @Override
                    public void write(char[] cbuf, int off, int len) {
                        // EMPTY
                    }

                    @Override
                    public void flush() {
                        // EMPTY
                    }

                    @Override
                    public void close() {
                        // EMPTY
                    }
                })
                .setLimitQueue(limitQueue);
        for (int i = 0; i < writerCount; i++) {
            writer.addFormatWriter(new TestWriter(i));
        }
    }

    @Benchmark
    public void write() {
        writer.writeSync();
    }
}
