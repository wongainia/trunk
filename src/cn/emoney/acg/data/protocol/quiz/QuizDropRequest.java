package cn.emoney.acg.data.protocol.quiz;
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: QuizDrop_Request.proto

public final class QuizDropRequest {
  private QuizDropRequest() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface QuizDrop_RequestOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // required string token_id = 1;
    /**
     * <code>required string token_id = 1;</code>
     */
    boolean hasTokenId();
    /**
     * <code>required string token_id = 1;</code>
     */
    java.lang.String getTokenId();
    /**
     * <code>required string token_id = 1;</code>
     */
    com.google.protobuf.ByteString
        getTokenIdBytes();

    // required uint64 id = 2;
    /**
     * <code>required uint64 id = 2;</code>
     *
     * <pre>
     *要放弃的问题
     * </pre>
     */
    boolean hasId();
    /**
     * <code>required uint64 id = 2;</code>
     *
     * <pre>
     *要放弃的问题
     * </pre>
     */
    long getId();
  }
  /**
   * Protobuf type {@code quiz.QuizDrop_Request}
   *
   * <pre>
   *抢到之后放弃回答
   * </pre>
   */
  public static final class QuizDrop_Request extends
      com.google.protobuf.GeneratedMessage
      implements QuizDrop_RequestOrBuilder {
    // Use QuizDrop_Request.newBuilder() to construct.
    private QuizDrop_Request(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private QuizDrop_Request(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final QuizDrop_Request defaultInstance;
    public static QuizDrop_Request getDefaultInstance() {
      return defaultInstance;
    }

    public QuizDrop_Request getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private QuizDrop_Request(
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
              tokenId_ = input.readBytes();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              id_ = input.readUInt64();
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
      return QuizDropRequest.internal_static_quiz_QuizDrop_Request_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return QuizDropRequest.internal_static_quiz_QuizDrop_Request_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              QuizDropRequest.QuizDrop_Request.class, QuizDropRequest.QuizDrop_Request.Builder.class);
    }

    public static com.google.protobuf.Parser<QuizDrop_Request> PARSER =
        new com.google.protobuf.AbstractParser<QuizDrop_Request>() {
      public QuizDrop_Request parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new QuizDrop_Request(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<QuizDrop_Request> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required string token_id = 1;
    public static final int TOKEN_ID_FIELD_NUMBER = 1;
    private java.lang.Object tokenId_;
    /**
     * <code>required string token_id = 1;</code>
     */
    public boolean hasTokenId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string token_id = 1;</code>
     */
    public java.lang.String getTokenId() {
      java.lang.Object ref = tokenId_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          tokenId_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string token_id = 1;</code>
     */
    public com.google.protobuf.ByteString
        getTokenIdBytes() {
      java.lang.Object ref = tokenId_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        tokenId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    // required uint64 id = 2;
    public static final int ID_FIELD_NUMBER = 2;
    private long id_;
    /**
     * <code>required uint64 id = 2;</code>
     *
     * <pre>
     *要放弃的问题
     * </pre>
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required uint64 id = 2;</code>
     *
     * <pre>
     *要放弃的问题
     * </pre>
     */
    public long getId() {
      return id_;
    }

    private void initFields() {
      tokenId_ = "";
      id_ = 0L;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasTokenId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasId()) {
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
        output.writeBytes(1, getTokenIdBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeUInt64(2, id_);
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
          .computeBytesSize(1, getTokenIdBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(2, id_);
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

    public static QuizDropRequest.QuizDrop_Request parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static QuizDropRequest.QuizDrop_Request parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static QuizDropRequest.QuizDrop_Request parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static QuizDropRequest.QuizDrop_Request parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static QuizDropRequest.QuizDrop_Request parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static QuizDropRequest.QuizDrop_Request parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static QuizDropRequest.QuizDrop_Request parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static QuizDropRequest.QuizDrop_Request parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static QuizDropRequest.QuizDrop_Request parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static QuizDropRequest.QuizDrop_Request parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(QuizDropRequest.QuizDrop_Request prototype) {
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
     * Protobuf type {@code quiz.QuizDrop_Request}
     *
     * <pre>
     *抢到之后放弃回答
     * </pre>
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements QuizDropRequest.QuizDrop_RequestOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return QuizDropRequest.internal_static_quiz_QuizDrop_Request_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return QuizDropRequest.internal_static_quiz_QuizDrop_Request_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                QuizDropRequest.QuizDrop_Request.class, QuizDropRequest.QuizDrop_Request.Builder.class);
      }

      // Construct using QuizDropRequest.QuizDrop_Request.newBuilder()
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
        tokenId_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        id_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return QuizDropRequest.internal_static_quiz_QuizDrop_Request_descriptor;
      }

      public QuizDropRequest.QuizDrop_Request getDefaultInstanceForType() {
        return QuizDropRequest.QuizDrop_Request.getDefaultInstance();
      }

      public QuizDropRequest.QuizDrop_Request build() {
        QuizDropRequest.QuizDrop_Request result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public QuizDropRequest.QuizDrop_Request buildPartial() {
        QuizDropRequest.QuizDrop_Request result = new QuizDropRequest.QuizDrop_Request(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.tokenId_ = tokenId_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.id_ = id_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof QuizDropRequest.QuizDrop_Request) {
          return mergeFrom((QuizDropRequest.QuizDrop_Request)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(QuizDropRequest.QuizDrop_Request other) {
        if (other == QuizDropRequest.QuizDrop_Request.getDefaultInstance()) return this;
        if (other.hasTokenId()) {
          bitField0_ |= 0x00000001;
          tokenId_ = other.tokenId_;
          onChanged();
        }
        if (other.hasId()) {
          setId(other.getId());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasTokenId()) {
          
          return false;
        }
        if (!hasId()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        QuizDropRequest.QuizDrop_Request parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (QuizDropRequest.QuizDrop_Request) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required string token_id = 1;
      private java.lang.Object tokenId_ = "";
      /**
       * <code>required string token_id = 1;</code>
       */
      public boolean hasTokenId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string token_id = 1;</code>
       */
      public java.lang.String getTokenId() {
        java.lang.Object ref = tokenId_;
        if (!(ref instanceof java.lang.String)) {
          java.lang.String s = ((com.google.protobuf.ByteString) ref)
              .toStringUtf8();
          tokenId_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string token_id = 1;</code>
       */
      public com.google.protobuf.ByteString
          getTokenIdBytes() {
        java.lang.Object ref = tokenId_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          tokenId_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string token_id = 1;</code>
       */
      public Builder setTokenId(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        tokenId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string token_id = 1;</code>
       */
      public Builder clearTokenId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        tokenId_ = getDefaultInstance().getTokenId();
        onChanged();
        return this;
      }
      /**
       * <code>required string token_id = 1;</code>
       */
      public Builder setTokenIdBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        tokenId_ = value;
        onChanged();
        return this;
      }

      // required uint64 id = 2;
      private long id_ ;
      /**
       * <code>required uint64 id = 2;</code>
       *
       * <pre>
       *要放弃的问题
       * </pre>
       */
      public boolean hasId() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required uint64 id = 2;</code>
       *
       * <pre>
       *要放弃的问题
       * </pre>
       */
      public long getId() {
        return id_;
      }
      /**
       * <code>required uint64 id = 2;</code>
       *
       * <pre>
       *要放弃的问题
       * </pre>
       */
      public Builder setId(long value) {
        bitField0_ |= 0x00000002;
        id_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required uint64 id = 2;</code>
       *
       * <pre>
       *要放弃的问题
       * </pre>
       */
      public Builder clearId() {
        bitField0_ = (bitField0_ & ~0x00000002);
        id_ = 0L;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:quiz.QuizDrop_Request)
    }

    static {
      defaultInstance = new QuizDrop_Request(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:quiz.QuizDrop_Request)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_quiz_QuizDrop_Request_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_quiz_QuizDrop_Request_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\026QuizDrop_Request.proto\022\004quiz\"0\n\020QuizDr" +
      "op_Request\022\020\n\010token_id\030\001 \002(\t\022\n\n\002id\030\002 \002(\004" +
      "B\002\n\000"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_quiz_QuizDrop_Request_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_quiz_QuizDrop_Request_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_quiz_QuizDrop_Request_descriptor,
              new java.lang.String[] { "TokenId", "Id", });
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
