package cn.emoney.acg.data.protocol.info;

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: GlobalMessage.proto

public final class GlobalMessage {
  private GlobalMessage() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface MessageCommonOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // required string msg_data = 1;
    /**
     * <code>required string msg_data = 1;</code>
     */
    boolean hasMsgData();
    /**
     * <code>required string msg_data = 1;</code>
     */
    java.lang.String getMsgData();
    /**
     * <code>required string msg_data = 1;</code>
     */
    com.google.protobuf.ByteString
        getMsgDataBytes();
  }
  /**
   * Protobuf type {@code MessageCommon}
   */
  public static final class MessageCommon extends
      com.google.protobuf.GeneratedMessage
      implements MessageCommonOrBuilder {
    // Use MessageCommon.newBuilder() to construct.
    private MessageCommon(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private MessageCommon(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final MessageCommon defaultInstance;
    public static MessageCommon getDefaultInstance() {
      return defaultInstance;
    }

    public MessageCommon getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private MessageCommon(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              bitField0_ |= 0x00000001;
              msgData_ = input.readBytes();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return GlobalMessage.internal_static_MessageCommon_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return GlobalMessage.internal_static_MessageCommon_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              GlobalMessage.MessageCommon.class, GlobalMessage.MessageCommon.Builder.class);
    }

    public static com.google.protobuf.Parser<MessageCommon> PARSER =
        new com.google.protobuf.AbstractParser<MessageCommon>() {
      public MessageCommon parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new MessageCommon(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<MessageCommon> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required string msg_data = 1;
    public static final int MSG_DATA_FIELD_NUMBER = 1;
    private java.lang.Object msgData_;
    /**
     * <code>required string msg_data = 1;</code>
     */
    public boolean hasMsgData() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string msg_data = 1;</code>
     */
    public java.lang.String getMsgData() {
      java.lang.Object ref = msgData_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          msgData_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string msg_data = 1;</code>
     */
    public com.google.protobuf.ByteString
        getMsgDataBytes() {
      java.lang.Object ref = msgData_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        msgData_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private void initFields() {
      msgData_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasMsgData()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getMsgDataBytes());
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getMsgDataBytes());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static GlobalMessage.MessageCommon parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static GlobalMessage.MessageCommon parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static GlobalMessage.MessageCommon parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static GlobalMessage.MessageCommon parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static GlobalMessage.MessageCommon parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static GlobalMessage.MessageCommon parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static GlobalMessage.MessageCommon parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static GlobalMessage.MessageCommon parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static GlobalMessage.MessageCommon parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static GlobalMessage.MessageCommon parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(GlobalMessage.MessageCommon prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code MessageCommon}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements GlobalMessage.MessageCommonOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return GlobalMessage.internal_static_MessageCommon_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return GlobalMessage.internal_static_MessageCommon_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                GlobalMessage.MessageCommon.class, GlobalMessage.MessageCommon.Builder.class);
      }

      // Construct using GlobalMessage.MessageCommon.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        msgData_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return GlobalMessage.internal_static_MessageCommon_descriptor;
      }

      public GlobalMessage.MessageCommon getDefaultInstanceForType() {
        return GlobalMessage.MessageCommon.getDefaultInstance();
      }

      public GlobalMessage.MessageCommon build() {
        GlobalMessage.MessageCommon result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public GlobalMessage.MessageCommon buildPartial() {
        GlobalMessage.MessageCommon result = new GlobalMessage.MessageCommon(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.msgData_ = msgData_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof GlobalMessage.MessageCommon) {
          return mergeFrom((GlobalMessage.MessageCommon)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(GlobalMessage.MessageCommon other) {
        if (other == GlobalMessage.MessageCommon.getDefaultInstance()) return this;
        if (other.hasMsgData()) {
          bitField0_ |= 0x00000001;
          msgData_ = other.msgData_;
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasMsgData()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        GlobalMessage.MessageCommon parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (GlobalMessage.MessageCommon) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required string msg_data = 1;
      private java.lang.Object msgData_ = "";
      /**
       * <code>required string msg_data = 1;</code>
       */
      public boolean hasMsgData() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string msg_data = 1;</code>
       */
      public java.lang.String getMsgData() {
        java.lang.Object ref = msgData_;
        if (!(ref instanceof java.lang.String)) {
          java.lang.String s = ((com.google.protobuf.ByteString) ref)
              .toStringUtf8();
          msgData_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string msg_data = 1;</code>
       */
      public com.google.protobuf.ByteString
          getMsgDataBytes() {
        java.lang.Object ref = msgData_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          msgData_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string msg_data = 1;</code>
       */
      public Builder setMsgData(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        msgData_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string msg_data = 1;</code>
       */
      public Builder clearMsgData() {
        bitField0_ = (bitField0_ & ~0x00000001);
        msgData_ = getDefaultInstance().getMsgData();
        onChanged();
        return this;
      }
      /**
       * <code>required string msg_data = 1;</code>
       */
      public Builder setMsgDataBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        msgData_ = value;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:MessageCommon)
    }

    static {
      defaultInstance = new MessageCommon(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:MessageCommon)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_MessageCommon_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_MessageCommon_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023GlobalMessage.proto\"!\n\rMessageCommon\022\020" +
      "\n\010msg_data\030\001 \002(\t"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_MessageCommon_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_MessageCommon_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_MessageCommon_descriptor,
              new java.lang.String[] { "MsgData", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
