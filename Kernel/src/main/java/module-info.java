module com.potato.kernel {
    requires com.google.gson;
    requires jdk.security.auth;
    requires jdk.compiler;

    exports com.potato.kernel;
    exports com.potato.kernel.External;
    exports com.potato.kernel.Hardware;
    exports com.potato.kernel.Software;
    exports com.potato.kernel.Utils;

    opens com.potato.kernel.Hardware to com.google.gson;
}