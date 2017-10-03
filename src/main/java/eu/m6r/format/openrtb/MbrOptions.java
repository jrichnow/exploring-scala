// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: mbr-options.proto

package eu.m6r.format.openrtb;

public final class MbrOptions {
  private MbrOptions() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registry.add(eu.m6r.format.openrtb.MbrOptions.parser);
  }
  /**
   * Protobuf enum {@code eu.m6r.format.openrtb.Parser}
   */
  public enum Parser
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>timestamp = 1;</code>
     */
    timestamp(0, 1),
    /**
     * <code>json = 2;</code>
     */
    json(1, 2),
    ;

    /**
     * <code>timestamp = 1;</code>
     */
    public static final int timestamp_VALUE = 1;
    /**
     * <code>json = 2;</code>
     */
    public static final int json_VALUE = 2;


    public final int getNumber() { return value; }

    public static Parser valueOf(int value) {
      switch (value) {
        case 1: return timestamp;
        case 2: return json;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Parser>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<Parser>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Parser>() {
            public Parser findValueByNumber(int number) {
              return Parser.valueOf(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return eu.m6r.format.openrtb.MbrOptions.getDescriptor().getEnumTypes().get(0);
    }

    private static final Parser[] VALUES = values();

    public static Parser valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }

    private final int index;
    private final int value;

    private Parser(int index, int value) {
      this.index = index;
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:eu.m6r.format.openrtb.Parser)
  }

  public static final int PARSER_FIELD_NUMBER = 50000;
  /**
   * <code>extend .google.protobuf.FieldOptions { ... }</code>
   */
  public static final
    com.google.protobuf.GeneratedMessage.GeneratedExtension<
      com.google.protobuf.DescriptorProtos.FieldOptions,
      eu.m6r.format.openrtb.MbrOptions.Parser> parser = com.google.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        eu.m6r.format.openrtb.MbrOptions.Parser.class,
        null);

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\021mbr-options.proto\022\025eu.m6r.format.openr" +
      "tb\032 google/protobuf/descriptor.proto*!\n\006" +
      "Parser\022\r\n\ttimestamp\020\001\022\010\n\004json\020\002:N\n\006parse" +
      "r\022\035.google.protobuf.FieldOptions\030\320\206\003 \001(\016" +
      "2\035.eu.m6r.format.openrtb.Parser"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.DescriptorProtos.getDescriptor(),
        }, assigner);
    parser.internalInit(descriptor.getExtensions().get(0));
    com.google.protobuf.DescriptorProtos.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}