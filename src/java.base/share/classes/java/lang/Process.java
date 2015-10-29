/*
 * Copyright (c) 1995, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * {@code Process} provides control of native processes started by
 * ProcessBuilder.start and Runtime.exec.
 * The class provides methods for performing input from the process, performing
 * output to the process, waiting for the process to complete,
 * checking the exit status of the process, and destroying (killing)
 * the process.
 * The {@link ProcessBuilder#start()} and
 * {@link Runtime#exec(String[],String[],File) Runtime.exec}
 * methods create a native process and return an instance of a
 * subclass of {@code Process} that can be used to control the process
 * and obtain information about it.
 *
 * <p>The methods that create processes may not work well for special
 * processes on certain native platforms, such as native windowing
 * processes, daemon processes, Win16/DOS processes on Microsoft
 * Windows, or shell scripts.
 *
 * <p>By default, the created process does not have its own terminal
 * or console.  All its standard I/O (i.e. stdin, stdout, stderr)
 * operations will be redirected to the parent process, where they can
 * be accessed via the streams obtained using the methods
 * {@link #getOutputStream()},
 * {@link #getInputStream()}, and
 * {@link #getErrorStream()}.
 * The parent process uses these streams to feed input to and get output
 * from the process.  Because some native platforms only provide
 * limited buffer size for standard input and output streams, failure
 * to promptly write the input stream or read the output stream of
 * the process may cause the process to block, or even deadlock.
 *
 * <p>Where desired, <a href="ProcessBuilder.html#redirect-input">
 * process I/O can also be redirected</a>
 * using methods of the {@link ProcessBuilder} class.
 *
 * <p>The process is not killed when there are no more references to
 * the {@code Process} object, but rather the process
 * continues executing asynchronously.
 *
 * <p>There is no requirement that the process represented by a {@code
 * Process} object execute asynchronously or concurrently with respect
 * to the Java process that owns the {@code Process} object.
 *
 * <p>As of 1.5, {@link ProcessBuilder#start()} is the preferred way
 * to create a {@code Process}.
 *
 * <p>Subclasses of Process should override the {@link #onExit()} and
 * {@link #toHandle()} methods to provide a fully functional Process including the
 * {@link #getPid() process id},
 * {@link #info() information about the process},
 * {@link #children() direct children}, and
 * {@link #allChildren() direct and indirect children} of the process.
 * Delegating to the underlying Process or ProcessHandle is typically
 * easiest and most efficient.
 *
 * @since   1.0
 */
public abstract class Process {
    /**
     * Default constructor for Process.
     */
    public Process() {}

    /**
     * Returns the output stream connected to the normal input of the
     * process.  Output to the stream is piped into the standard
     * input of the process represented by this {@code Process} object.
     *
     * <p>If the standard input of the process has been redirected using
     * {@link ProcessBuilder#redirectInput(Redirect)
     * ProcessBuilder.redirectInput}
     * then this method will return a
     * <a href="ProcessBuilder.html#redirect-input">null output stream</a>.
     *
     * <p>Implementation note: It is a good idea for the returned
     * output stream to be buffered.
     *
     * @return the output stream connected to the normal input of the
     *         process
     */
    public abstract OutputStream getOutputStream();

    /**
     * Returns the input stream connected to the normal output of the
     * process.  The stream obtains data piped from the standard
     * output of the process represented by this {@code Process} object.
     *
     * <p>If the standard output of the process has been redirected using
     * {@link ProcessBuilder#redirectOutput(Redirect)
     * ProcessBuilder.redirectOutput}
     * then this method will return a
     * <a href="ProcessBuilder.html#redirect-output">null input stream</a>.
     *
     * <p>Otherwise, if the standard error of the process has been
     * redirected using
     * {@link ProcessBuilder#redirectErrorStream(boolean)
     * ProcessBuilder.redirectErrorStream}
     * then the input stream returned by this method will receive the
     * merged standard output and the standard error of the process.
     *
     * <p>Implementation note: It is a good idea for the returned
     * input stream to be buffered.
     *
     * @return the input stream connected to the normal output of the
     *         process
     */
    public abstract InputStream getInputStream();

    /**
     * Returns the input stream connected to the error output of the
     * process.  The stream obtains data piped from the error output
     * of the process represented by this {@code Process} object.
     *
     * <p>If the standard error of the process has been redirected using
     * {@link ProcessBuilder#redirectError(Redirect)
     * ProcessBuilder.redirectError} or
     * {@link ProcessBuilder#redirectErrorStream(boolean)
     * ProcessBuilder.redirectErrorStream}
     * then this method will return a
     * <a href="ProcessBuilder.html#redirect-output">null input stream</a>.
     *
     * <p>Implementation note: It is a good idea for the returned
     * input stream to be buffered.
     *
     * @return the input stream connected to the error output of
     *         the process
     */
    public abstract InputStream getErrorStream();

    /**
     * Causes the current thread to wait, if necessary, until the
     * process represented by this {@code Process} object has
     * terminated.  This method returns immediately if the process
     * has already terminated.  If the process has not yet
     * terminated, the calling thread will be blocked until the
     * process exits.
     *
     * @return the exit value of the process represented by this
     *         {@code Process} object.  By convention, the value
     *         {@code 0} indicates normal termination.
     * @throws InterruptedException if the current thread is
     *         {@linkplain Thread#interrupt() interrupted} by another
     *         thread while it is waiting, then the wait is ended and
     *         an {@link InterruptedException} is thrown.
     */
    public abstract int waitFor() throws InterruptedException;

    /**
     * Causes the current thread to wait, if necessary, until the
     * process represented by this {@code Process} object has
     * terminated, or the specified waiting time elapses.
     *
     * <p>If the process has already terminated then this method returns
     * immediately with the value {@code true}.  If the process has not
     * terminated and the timeout value is less than, or equal to, zero, then
     * this method returns immediately with the value {@code false}.
     *
     * <p>The default implementation of this methods polls the {@code exitValue}
     * to check if the process has terminated. Concrete implementations of this
     * class are strongly encouraged to override this method with a more
     * efficient implementation.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if the process has exited and {@code false} if
     *         the waiting time elapsed before the process has exited.
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting.
     * @throws NullPointerException if unit is null
     * @since 1.8
     */
    public boolean waitFor(long timeout, TimeUnit unit)
        throws InterruptedException
    {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do {
            try {
                exitValue();
                return true;
            } catch(IllegalThreadStateException ex) {
                if (rem > 0)
                    Thread.sleep(
                        Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return false;
    }

    /**
     * Returns the exit value for the process.
     *
     * @return the exit value of the process represented by this
     *         {@code Process} object.  By convention, the value
     *         {@code 0} indicates normal termination.
     * @throws IllegalThreadStateException if the process represented
     *         by this {@code Process} object has not yet terminated
     */
    public abstract int exitValue();

    /**
     * Kills the process.
     * Whether the process represented by this {@code Process} object is
     * {@link #supportsNormalTermination normally terminated} or not is
     * implementation dependent.
     * Forcible process destruction is defined as the immediate termination of a
     * process, whereas normal termination allows the process to shut down cleanly.
     * If the process is not alive, no action is taken.
     * <p>
     * The {@link java.util.concurrent.CompletableFuture} from {@link #onExit} is
     * {@link java.util.concurrent.CompletableFuture#complete completed}
     * when the process has terminated.
     */
    public abstract void destroy();

    /**
     * Kills the process forcibly. The process represented by this
     * {@code Process} object is forcibly terminated.
     * Forcible process destruction is defined as the immediate termination of a
     * process, whereas normal termination allows the process to shut down cleanly.
     * If the process is not alive, no action is taken.
     * <p>
     * The {@link java.util.concurrent.CompletableFuture} from {@link #onExit} is
     * {@link java.util.concurrent.CompletableFuture#complete completed}
     * when the process has terminated.
     * <p>
     * Invoking this method on {@code Process} objects returned by
     * {@link ProcessBuilder#start} and {@link Runtime#exec} forcibly terminate
     * the process.
     *
     * @implSpec
     * The default implementation of this method invokes {@link #destroy}
     * and so may not forcibly terminate the process.
     * @implNote
     * Concrete implementations of this class are strongly encouraged to override
     * this method with a compliant implementation.
     * @apiNote
     * The process may not terminate immediately.
     * i.e. {@code isAlive()} may return true for a brief period
     * after {@code destroyForcibly()} is called. This method
     * may be chained to {@code waitFor()} if needed.
     *
     * @return the {@code Process} object representing the
     *         process forcibly destroyed
     * @since 1.8
     */
    public Process destroyForcibly() {
        destroy();
        return this;
    }

    /**
     * Returns {@code true} if the implementation of {@link #destroy} is to
     * normally terminate the process,
     * Returns {@code false} if the implementation of {@code destroy}
     * forcibly and immediately terminates the process.
     * <p>
     * Invoking this method on {@code Process} objects returned by
     * {@link ProcessBuilder#start} and {@link Runtime#exec} return
     * {@code true} or {@code false} depending on the platform implementation.
     *
     * @implSpec
     * This implementation throws an instance of
     * {@link java.lang.UnsupportedOperationException} and performs no other action.
     *
     * @return {@code true} if the implementation of {@link #destroy} is to
     *         normally terminate the process;
     *         otherwise, {@link #destroy} forcibly terminates the process
     * @throws UnsupportedOperationException if the Process implementation
     *         does not support this operation
     * @since 1.9
     */
    public boolean supportsNormalTermination() {
        throw new UnsupportedOperationException(this.getClass()
                + ".supportsNormalTermination() not supported" );
    }

    /**
     * Tests whether the process represented by this {@code Process} is
     * alive.
     *
     * @return {@code true} if the process represented by this
     *         {@code Process} object has not yet terminated.
     * @since 1.8
     */
    public boolean isAlive() {
        try {
            exitValue();
            return false;
        } catch(IllegalThreadStateException e) {
            return true;
        }
    }

    /**
     * Returns the native process ID of the process.
     * The native process ID is an identification number that the operating
     * system assigns to the process.
     *
     * @implSpec
     * The implementation of this method returns the process id as:
     * {@link #toHandle toHandle().getPid()}.
     *
     * @return the native process id of the process
     * @throws UnsupportedOperationException if the Process implementation
     *         does not support this operation
     * @since 1.9
     */
    public long getPid() {
        return toHandle().getPid();
    }

    /**
     * Returns a {@code CompletableFuture<Process>} for the termination of the Process.
     * The {@link java.util.concurrent.CompletableFuture} provides the ability
     * to trigger dependent functions or actions that may be run synchronously
     * or asynchronously upon process termination.
     * When the process terminates the CompletableFuture is
     * {@link java.util.concurrent.CompletableFuture#complete completed} regardless
     * of the exit status of the process.
     * <p>
     * Calling {@code onExit().get()} waits for the process to terminate and returns
     * the Process. The future can be used to check if the process is
     * {@link java.util.concurrent.CompletableFuture#isDone done} or to
     * {@link java.util.concurrent.CompletableFuture#get() wait} for it to terminate.
     * {@link java.util.concurrent.CompletableFuture#cancel(boolean) Cancelling}
     * the CompletableFuture does not affect the Process.
     * <p>
     * If the process is {@link #isAlive not alive} the {@link CompletableFuture}
     * returned has been {@link java.util.concurrent.CompletableFuture#complete completed}.
     * <p>
     * Processes returned from {@link ProcessBuilder#start} override the
     * default implementation to provide an efficient mechanism to wait
     * for process exit.
     *
     * @apiNote
     * Using {@link #onExit() onExit} is an alternative to
     * {@link #waitFor() waitFor} that enables both additional concurrency
     * and convenient access to the result of the Process.
     * Lambda expressions can be used to evaluate the result of the Process
     * execution.
     * If there is other processing to be done before the value is used
     * then {@linkplain #onExit onExit} is a convenient mechanism to
     * free the current thread and block only if and when the value is needed.
     * <br>
     * For example, launching a process to compare two files and get a boolean if they are identical:
     * <pre> {@code   Process p = new ProcessBuilder("cmp", "f1", "f2").start();
     *    Future<Boolean> identical = p.onExit().thenApply(p1 -> p1.exitValue() == 0);
     *    ...
     *    if (identical.get()) { ... }
     * }</pre>
     *
     * @implSpec
     * This implementation executes {@link #waitFor()} in a separate thread
     * repeatedly until it returns successfully. If the execution of
     * {@code waitFor} is interrupted, the thread's interrupt status is preserved.
     * <p>
     * When {@link #waitFor()} returns successfully the CompletableFuture is
     * {@link java.util.concurrent.CompletableFuture#complete completed} regardless
     * of the exit status of the process.
     *
     * This implementation may consume a lot of memory for thread stacks if a
     * large number of processes are waited for concurrently.
     * <p>
     * External implementations should override this method and provide
     * a more efficient implementation. For example, to delegate to the underlying
     * process, it can do the following:
     * <pre>{@code
     *    public CompletableFuture<Process> onExit() {
     *       return delegate.onExit().thenApply(p -> this);
     *    }
     * }</pre>
     *
     * @return a new {@code CompletableFuture<Process>} for the Process
     *
     * @since 1.9
     */
    public CompletableFuture<Process> onExit() {
        return CompletableFuture.supplyAsync(this::waitForInternal);
    }

    /**
     * Wait for the process to exit by calling {@code waitFor}.
     * If the thread is interrupted, remember the interrupted state to
     * be restored before returning. Use ForkJoinPool.ManagedBlocker
     * so that the number of workers in case ForkJoinPool is used is
     * compensated when the thread blocks in waitFor().
     *
     * @return the Process
     */
    private Process waitForInternal() {
        boolean interrupted = false;
        while (true) {
            try {
                ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
                    @Override
                    public boolean block() throws InterruptedException {
                        waitFor();
                        return true;
                    }

                    @Override
                    public boolean isReleasable() {
                        return !isAlive();
                    }
                });
                break;
            } catch (InterruptedException x) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    /**
     * Returns a ProcessHandle for the Process.
     *
     * {@code Process} objects returned by {@link ProcessBuilder#start} and
     * {@link Runtime#exec} implement {@code toHandle} as the equivalent of
     * {@link ProcessHandle#of(long) ProcessHandle.of(pid)} including the
     * check for a SecurityManager and {@code RuntimePermission("manageProcess")}.
     *
     * @implSpec
     * This implementation throws an instance of
     * {@link java.lang.UnsupportedOperationException} and performs no other action.
     * Subclasses should override this method to provide a ProcessHandle for the
     * process.  The methods {@link #getPid}, {@link #info}, {@link #children},
     * and {@link #allChildren}, unless overridden, operate on the ProcessHandle.
     *
     * @return Returns a ProcessHandle for the Process
     * @throws UnsupportedOperationException if the Process implementation
     *         does not support this operation
     * @throws SecurityException if a security manager has been installed and
     *         it denies RuntimePermission("manageProcess")
     * @since 1.9
     */
    public ProcessHandle toHandle() {
        throw new UnsupportedOperationException(this.getClass()
                + ".toHandle() not supported");
    }

    /**
     * Returns a snapshot of information about the process.
     *
     * <p> An {@link ProcessHandle.Info} instance has various accessor methods
     * that return information about the process, if the process is alive and
     * the information is available, otherwise {@code null} is returned.
     *
     * @implSpec
     * This implementation returns information about the process as:
     * {@link #toHandle toHandle().info()}.
     *
     * @return a snapshot of information about the process, always non-null
     * @throws UnsupportedOperationException if the Process implementation
     *         does not support this operation
     * @since 1.9
     */
    public ProcessHandle.Info info() {
        return toHandle().info();
    }

    /**
     * Returns a snapshot of the direct children of the process.
     * The parent of a direct child process is the process.
     * Typically, a process that is {@link #isAlive not alive} has no children.
     * <p>
     * <em>Note that processes are created and terminate asynchronously.
     * There is no guarantee that a process is {@link #isAlive alive}.
     * </em>
     *
     * @implSpec
     * This implementation returns the direct children as:
     * {@link #toHandle toHandle().children()}.
     *
     * @return a sequential Stream of ProcessHandles for processes that are
     *         direct children of the process
     * @throws UnsupportedOperationException if the Process implementation
     *         does not support this operation
     * @throws SecurityException if a security manager has been installed and
     *         it denies RuntimePermission("manageProcess")
     * @since 1.9
     */
    public Stream<ProcessHandle> children() {
        return toHandle().children();
    }

    /**
     * Returns a snapshot of the direct and indirect children of the process.
     * An indirect child is one whose parent is either a direct child or
     * another indirect child.
     * Typically, a process that is {@link #isAlive not alive} has no children.
     * <p>
     * <em>Note that processes are created and terminate asynchronously.
     * There is no guarantee that a process is {@link #isAlive alive}.
     * </em>
     *
     * @implSpec
     * This implementation returns all children as:
     * {@link #toHandle toHandle().allChildren()}.
     *
     * @return a sequential Stream of ProcessHandles for processes that are
     *         direct and indirect children of the process
     * @throws UnsupportedOperationException if the Process implementation
     *         does not support this operation
     * @throws SecurityException if a security manager has been installed and
     *         it denies RuntimePermission("manageProcess")
     * @since 1.9
     */
    public Stream<ProcessHandle> allChildren() {
        return toHandle().allChildren();
    }


}