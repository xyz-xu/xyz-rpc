package xyz.rpc.framework;

import org.springframework.core.io.AbstractResource;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * rpc远程调用 resource
 *
 * @author xin.xu
 */
public class RpcCallResource extends AbstractResource {

    private final InputStream inputStream;
    private final byte[] bytes;
    private final String fileName;

    public RpcCallResource(InputStream inputStream, String fileName) {
        this.inputStream = Objects.requireNonNull(inputStream);
        this.bytes = null;

        Assert.hasText(fileName, "blank file name");
        this.fileName = fileName;
    }

    public RpcCallResource(byte[] bytes, String fileName) {
        this.inputStream = null;
        this.bytes = bytes;

        Assert.hasText(fileName, "blank file name");
        this.fileName = fileName;
    }

    /**
     * Return a description for this resource,
     * to be used for error output when working with the resource.
     * <p>Implementations are also encouraged to return this value
     * from their {@code toString} method.
     *
     * @see Object#toString()
     */
    @Override
    public String getDescription() {
        return "stream of " + fileName;
    }

    /**
     * Return an {@link InputStream} for the content of an underlying resource.
     * <p>It is expected that each call creates a <i>fresh</i> stream.
     * <p>This requirement is particularly important when you consider an API such
     * as JavaMail, which needs to be able to read the stream multiple times when
     * creating mail attachments. For such a use case, it is <i>required</i>
     * that each {@code getInputStream()} call returns a fresh stream.
     *
     * @return the input stream for the underlying resource (must not be {@code null})
     */
    @Override
    public InputStream getInputStream() {
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }

        return inputStream;
    }

    public byte[] getBytes0() {
        return bytes;
    }

    @Override
    public String getFilename() {
        return fileName;
    }
}
