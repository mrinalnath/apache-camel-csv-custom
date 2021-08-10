/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dataformat.csv;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataFormatName;
import org.apache.camel.spi.annotations.Dataformat;
import org.apache.camel.support.service.ServiceSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

/**
 * CSV Data format.
 * <p/>
 * By default, columns are autogenerated in the resulting CSV. Subsequent messages use the previously created columns
 * with new fields being added at the end of the line. Thus, field order is the same from message to message.
 * Autogeneration can be disabled. In this case, only the fields defined in csvConfig are written on the output.
 */
@Dataformat("csv")
public class CsvDataFormat extends ServiceSupport implements DataFormat, DataFormatName {
    // CSV format options
    private CSVFormat format = CSVFormat.DEFAULT;
    private boolean commentMarkerDisabled;
    private Character commentMarker;
    private Character delimiter;
    private boolean escapeDisabled;
    private Character escape;
    private boolean headerDisabled;
    private String[] header;
    private Boolean allowMissingColumnNames;
    private Boolean ignoreEmptyLines;
    private Boolean ignoreSurroundingSpaces;
    private boolean nullStringDisabled;
    private String nullString;
    private boolean quoteDisabled;
    private Character quote;
    private QuoteMode quoteMode;
    private boolean recordSeparatorDisabled;
    private String recordSeparator;
    private Boolean skipHeaderRecord;
    private Boolean trim;
    private Boolean ignoreHeaderCase;
    private Boolean trailingDelimiter;

    // Unmarshal options
    private boolean lazyLoad;
    private boolean useMaps;
    private boolean useOrderedMaps;
    private CsvRecordConverter<?> recordConverter;

    private CsvMarshallerFactory marshallerFactory = CsvMarshallerFactory.DEFAULT;

    private volatile CsvMarshaller marshaller;
    private volatile CsvUnmarshaller unmarshaller;

    public CsvDataFormat() {
    }

    public CsvDataFormat(CSVFormat format) {
        setFormat(format);
    }

    @Override
    public String getDataFormatName() {
        return "csv";
    }

    @Override
    public void marshal(Exchange exchange, Object object, OutputStream outputStream) throws Exception {
        marshaller.marshal(exchange, object, outputStream);
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream inputStream) throws Exception {
        return unmarshaller.unmarshal(exchange, inputStream);
    }

    @Override
    protected void doInit() throws Exception {
        super.doInit();
        marshaller = marshallerFactory.create(getActiveFormat(), this);
        unmarshaller = CsvUnmarshaller.create(getActiveFormat(), this);
    }

    @Override
    protected void doStop() throws Exception {
        // noop
    }

    CSVFormat getActiveFormat() {
        CSVFormat answer = format;

        if (commentMarkerDisabled) {
            answer = answer.withCommentMarker(null); // null disables the comment marker
        } else if (commentMarker != null) {
            answer = answer.withCommentMarker(commentMarker);
        }

        if (delimiter != null) {
            answer = answer.withDelimiter(delimiter);
        }

        if (escapeDisabled) {
            answer = answer.withEscape(null); // null disables the escape
        } else if (escape != null) {
            answer = answer.withEscape(escape);
        }

        if (headerDisabled) {
            answer = answer.withHeader((String[]) null); // null disables the header
        } else if (header != null) {
            answer = answer.withHeader(header);
        }

        if (allowMissingColumnNames != null) {
            answer = answer.withAllowMissingColumnNames(allowMissingColumnNames);
        }

        if (ignoreEmptyLines != null) {
            answer = answer.withIgnoreEmptyLines(ignoreEmptyLines);
        }

        if (ignoreSurroundingSpaces != null) {
            answer = answer.withIgnoreSurroundingSpaces(ignoreSurroundingSpaces);
        }

        if (nullStringDisabled) {
            answer = answer.withNullString(null); // null disables the null string replacement
        } else if (nullString != null) {
            answer = answer.withNullString(nullString);
        }

        if (quoteDisabled) {
            answer = answer.withQuote(null); // null disables quotes
        } else if (quote != null) {
            answer = answer.withQuote(quote);
        }

        if (quoteMode != null) {
            answer = answer.withQuoteMode(quoteMode);
        }

        if (recordSeparatorDisabled) {
            answer = answer.withRecordSeparator(null); // null disables the record separator
        } else if (recordSeparator != null) {
            answer = answer.withRecordSeparator(recordSeparator);
        }

        if (skipHeaderRecord != null) {
            answer = answer.withSkipHeaderRecord(skipHeaderRecord);
        }

        if (trim != null) {
            answer = answer.withTrim(trim);
        }

        if (ignoreHeaderCase != null) {
            answer = answer.withIgnoreHeaderCase(ignoreHeaderCase);
        }

        if (trailingDelimiter != null) {
            answer = answer.withTrailingDelimiter(trailingDelimiter);
        }

        return answer;
    }

    //region Getters/Setters

    /**
     * Gets the CSV format before applying any changes. It cannot be {@code null}, the default one is
     * {@link org.apache.commons.csv.CSVFormat#DEFAULT}.
     *
     * @return CSV format
     */
    public CSVFormat getFormat() {
        return format;
    }

    /**
     * Sets the CSV format before applying any changes. If {@code null}, then
     * {@link org.apache.commons.csv.CSVFormat#DEFAULT} is used instead.
     *
     * @param  format CSV format
     * @return        Current {@code CsvDataFormat}, fluent API
     * @see           org.apache.commons.csv.CSVFormat
     * @see           org.apache.commons.csv.CSVFormat#DEFAULT
     */
    public CsvDataFormat setFormat(CSVFormat format) {
        this.format = (format == null) ? CSVFormat.DEFAULT : format;
        return this;
    }

    /**
     * Sets the {@link CsvMarshaller} factory. If {@code null}, then {@link CsvMarshallerFactory#DEFAULT} is used
     * instead.
     *
     * @param  marshallerFactory
     * @return                   Current {@code CsvDataFormat}, fluent API
     */
    public CsvDataFormat setMarshallerFactory(CsvMarshallerFactory marshallerFactory) {
        this.marshallerFactory = (marshallerFactory == null) ? CsvMarshallerFactory.DEFAULT : marshallerFactory;
        return this;
    }

    /**
     * Returns the used {@link CsvMarshallerFactory}.
     *
     * @return never {@code null}.
     */
    public CsvMarshallerFactory getMarshallerFactory() {
        return marshallerFactory;
    }

    /**
     * Sets the CSV format by name before applying any changes.
     *
     * @param  name CSV format name
     * @return      Current {@code CsvDataFormat}, fluent API
     * @see         #setFormat(org.apache.commons.csv.CSVFormat)
     * @see         org.apache.commons.csv.CSVFormat
     */
    public CsvDataFormat setFormatName(String name) {
        if (name == null) {
            setFormat(null);
        } else if ("DEFAULT".equals(name)) {
            setFormat(CSVFormat.DEFAULT);
        } else if ("RFC4180".equals(name)) {
            setFormat(CSVFormat.RFC4180);
        } else if ("EXCEL".equals(name)) {
            setFormat(CSVFormat.EXCEL);
        } else if ("TDF".equals(name)) {
            setFormat(CSVFormat.TDF);
        } else if ("MYSQL".equals(name)) {
            setFormat(CSVFormat.MYSQL);
        } else {
            throw new IllegalArgumentException("Unsupported format");
        }
        return this;
    }

    /**
     * Indicates whether or not the comment markers are disabled.
     *
     * @return {@code true} if the comment markers are disabled, {@code false} otherwise
     */
    public boolean isCommentMarkerDisabled() {
        return commentMarkerDisabled;
    }

    /**
     * Sets whether or not the comment markers are disabled.
     *
     * @param  commentMarkerDisabled {@code true} if the comment markers are disabled, {@code false} otherwise
     * @return                       Current {@code CsvDataFormat}, fluent API
     * @see                          org.apache.commons.csv.CSVFormat#withCommentMarker(java.lang.Character)
     */
    public CsvDataFormat setCommentMarkerDisabled(boolean commentMarkerDisabled) {
        this.commentMarkerDisabled = commentMarkerDisabled;
        return this;
    }

    /**
     * Gets the comment marker. If {@code null} then the default one of the format used.
     *
     * @return Comment marker
     */
    public Character getCommentMarker() {
        return commentMarker;
    }

    /**
     * Sets the comment marker to use. If {@code null} then the default one of the format used.
     *
     * @param  commentMarker Comment marker
     * @return               Current {@code CsvDataFormat}, fluent API
     * @see                  org.apache.commons.csv.CSVFormat#withCommentMarker(Character)
     */
    public CsvDataFormat setCommentMarker(Character commentMarker) {
        this.commentMarker = commentMarker;
        return this;
    }

    /**
     * Gets the delimiter. If {@code null} then the default one of the format used.
     *
     * @return Delimiter
     */
    public Character getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the delimiter. If {@code null} then the default one of the format used.
     *
     * @param  delimiter Delimiter
     * @return           Current {@code CsvDataFormat}, fluent API
     * @see              org.apache.commons.csv.CSVFormat#withDelimiter(char)
     */
    public CsvDataFormat setDelimiter(Character delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Indicates whether or not the escaping is disabled.
     *
     * @return {@code true} if the escaping is disabled, {@code false} otherwise
     */
    public boolean isEscapeDisabled() {
        return escapeDisabled;
    }

    /**
     * Sets whether or not the escaping is disabled.
     *
     * @param  escapeDisabled {@code true} if the escaping is disabled, {@code false} otherwise
     * @return                Current {@code CsvDataFormat}, fluent API
     * @see                   org.apache.commons.csv.CSVFormat#withEscape(Character)
     */
    public CsvDataFormat setEscapeDisabled(boolean escapeDisabled) {
        this.escapeDisabled = escapeDisabled;
        return this;
    }

    /**
     * Gets the escape character. If {@code null} then the default one of the format used.
     *
     * @return Escape character
     */
    public Character getEscape() {
        return escape;
    }

    /**
     * Sets the escape character. If {@code null} then the default one of the format used.
     *
     * @param  escape Escape character
     * @return        Current {@code CsvDataFormat}, fluent API
     * @see           org.apache.commons.csv.CSVFormat#withEscape(Character)
     */
    public CsvDataFormat setEscape(Character escape) {
        this.escape = escape;
        return this;
    }

    /**
     * Indicates whether or not the headers are disabled.
     *
     * @return {@code true} if the headers are disabled, {@code false} otherwise
     */
    public boolean isHeaderDisabled() {
        return headerDisabled;
    }

    /**
     * Sets whether or not the headers are disabled.
     *
     * @param  headerDisabled {@code true} if the headers are disabled, {@code false} otherwise
     * @return                Current {@code CsvDataFormat}, fluent API
     * @see                   org.apache.commons.csv.CSVFormat#withHeader(String...)
     */
    public CsvDataFormat setHeaderDisabled(boolean headerDisabled) {
        this.headerDisabled = headerDisabled;
        return this;
    }

    /**
     * Gets the header. If {@code null} then the default one of the format used. If empty then it will be automatically
     * handled.
     *
     * @return Header
     */
    public String[] getHeader() {
        return header;
    }

    /**
     * Gets the header. If {@code null} then the default one of the format used. If empty then it will be automatically
     * handled.
     *
     * @param  header Header
     * @return        Current {@code CsvDataFormat}, fluent API
     * @see           org.apache.commons.csv.CSVFormat#withHeader(String...)
     */
    public CsvDataFormat setHeader(String[] header) {
        this.header = Arrays.copyOf(header, header.length);
        return this;
    }

    /**
     * Indicates whether or not missing column names are allowed. If {@code null} then the default value of the format
     * used.
     *
     * @return Whether or not missing column names are allowed
     */
    public Boolean getAllowMissingColumnNames() {
        return allowMissingColumnNames;
    }

    /**
     * Sets whether or not missing column names are allowed. If {@code null} then the default value of the format used.
     *
     * @param  allowMissingColumnNames Whether or not missing column names are allowed
     * @return                         Current {@code CsvDataFormat}, fluent API
     * @see                            org.apache.commons.csv.CSVFormat#withAllowMissingColumnNames(boolean)
     */
    public CsvDataFormat setAllowMissingColumnNames(Boolean allowMissingColumnNames) {
        this.allowMissingColumnNames = allowMissingColumnNames;
        return this;
    }

    /**
     * Indicates whether or not empty lines must be ignored. If {@code null} then the default value of the format used.
     *
     * @return Whether or not empty lines must be ignored
     */
    public Boolean getIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    /**
     * Sets whether or not empty lines must be ignored. If {@code null} then the default value of the format used.
     *
     * @param  ignoreEmptyLines Whether or not empty lines must be ignored
     * @return                  Current {@code CsvDataFormat}, fluent API
     * @see                     org.apache.commons.csv.CSVFormat#withIgnoreEmptyLines(boolean)
     */
    public CsvDataFormat setIgnoreEmptyLines(Boolean ignoreEmptyLines) {
        this.ignoreEmptyLines = ignoreEmptyLines;
        return this;
    }

    /**
     * Indicates whether or not surrounding spaces must be ignored. If {@code null} then the default value of the format
     * used.
     *
     * @return Whether or not surrounding spaces must be ignored
     */
    public Boolean getIgnoreSurroundingSpaces() {
        return ignoreSurroundingSpaces;
    }

    /**
     * Sets whether or not surrounding spaces must be ignored. If {@code null} then the default value of the format
     * used.
     *
     * @param  ignoreSurroundingSpaces Whether or not surrounding spaces must be ignored
     * @return                         Current {@code CsvDataFormat}, fluent API
     * @see                            org.apache.commons.csv.CSVFormat#withIgnoreSurroundingSpaces(boolean)
     */
    public CsvDataFormat setIgnoreSurroundingSpaces(Boolean ignoreSurroundingSpaces) {
        this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
        return this;
    }

    /**
     * Indicates whether or not the null string replacement is disabled.
     *
     * @return {@code true} if the null string replacement is disabled, {@code false} otherwise
     */
    public boolean isNullStringDisabled() {
        return nullStringDisabled;
    }

    /**
     * Sets whether or not the null string replacement is disabled.
     *
     * @param  nullStringDisabled {@code true} if the null string replacement is disabled, {@code false} otherwise
     * @return                    Current {@code CsvDataFormat}, fluent API
     * @see                       org.apache.commons.csv.CSVFormat#withNullString(String)
     */
    public CsvDataFormat setNullStringDisabled(boolean nullStringDisabled) {
        this.nullStringDisabled = nullStringDisabled;
        return this;
    }

    /**
     * Gets the null string replacement. If {@code null} then the default one of the format used.
     *
     * @return Null string replacement
     */
    public String getNullString() {
        return nullString;
    }

    /**
     * Sets the null string replacement. If {@code null} then the default one of the format used.
     *
     * @param  nullString Null string replacement
     * @return            Current {@code CsvDataFormat}, fluent API
     * @see               org.apache.commons.csv.CSVFormat#withNullString(String)
     */
    public CsvDataFormat setNullString(String nullString) {
        this.nullString = nullString;
        return this;
    }

    /**
     * Indicates whether or not quotes are disabled.
     *
     * @return {@code true} if quotes are disabled, {@code false} otherwise
     */
    public boolean isQuoteDisabled() {
        return quoteDisabled;
    }

    /**
     * Sets whether or not quotes are disabled
     *
     * @param  quoteDisabled {@code true} if quotes are disabled, {@code false} otherwise
     * @return               Current {@code CsvDataFormat}, fluent API
     * @see                  org.apache.commons.csv.CSVFormat#withQuote(Character)
     */
    public CsvDataFormat setQuoteDisabled(boolean quoteDisabled) {
        this.quoteDisabled = quoteDisabled;
        return this;
    }

    /**
     * Gets the quote character. If {@code null} then the default one of the format used.
     *
     * @return Quote character
     */
    public Character getQuote() {
        return quote;
    }

    /**
     * Sets the quote character. If {@code null} then the default one of the format used.
     *
     * @param  quote Quote character
     * @return       Current {@code CsvDataFormat}, fluent API
     * @see          org.apache.commons.csv.CSVFormat#withQuote(Character)
     */
    public CsvDataFormat setQuote(Character quote) {
        this.quote = quote;
        return this;
    }

    /**
     * Gets the quote mode. If {@code null} then the default one of the format used.
     *
     * @return Quote mode
     */
    public QuoteMode getQuoteMode() {
        return quoteMode;
    }

    /**
     * Sets the quote mode. If {@code null} then the default one of the format used.
     *
     * @param  quoteMode Quote mode
     * @return           Current {@code CsvDataFormat}, fluent API
     * @see              org.apache.commons.csv.CSVFormat#withQuoteMode(org.apache.commons.csv.QuoteMode)
     */
    public CsvDataFormat setQuoteMode(QuoteMode quoteMode) {
        this.quoteMode = quoteMode;
        return this;
    }

    /**
     * Indicates whether or not the record separator is disabled.
     *
     * @return {@code true} if the record separator disabled, {@code false} otherwise
     */
    public boolean isRecordSeparatorDisabled() {
        return recordSeparatorDisabled;
    }

    /**
     * Sets whether or not the record separator is disabled.
     *
     * @param  recordSeparatorDisabled {@code true} if the record separator disabled, {@code false} otherwise
     * @return                         Current {@code CsvDataFormat}, fluent API
     * @see                            org.apache.commons.csv.CSVFormat#withRecordSeparator(String)
     */
    public CsvDataFormat setRecordSeparatorDisabled(boolean recordSeparatorDisabled) {
        this.recordSeparatorDisabled = recordSeparatorDisabled;
        return this;
    }

    /**
     * Gets the record separator. If {@code null} then the default one of the format used.
     *
     * @return Record separator
     */
    public String getRecordSeparator() {
        return recordSeparator;
    }

    /**
     * Sets the record separator. If {@code null} then the default one of the format used.
     *
     * @param  recordSeparator Record separator
     * @return                 Current {@code CsvDataFormat}, fluent API
     * @see                    org.apache.commons.csv.CSVFormat#withRecordSeparator(String)
     */
    public CsvDataFormat setRecordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }

    /**
     * Indicates whether or not header record must be skipped. If {@code null} then the default value of the format
     * used.
     *
     * @return Whether or not header record must be skipped
     */
    public Boolean getSkipHeaderRecord() {
        return skipHeaderRecord;
    }

    /**
     * Sets whether or not header record must be skipped. If {@code null} then the default value of the format used.
     *
     * @param  skipHeaderRecord Whether or not header record must be skipped
     * @return                  Current {@code CsvDataFormat}, fluent API
     * @see                     org.apache.commons.csv.CSVFormat#withSkipHeaderRecord(boolean)
     */
    public CsvDataFormat setSkipHeaderRecord(Boolean skipHeaderRecord) {
        this.skipHeaderRecord = skipHeaderRecord;
        return this;
    }

    /**
     * Indicates whether or not the unmarshalling should lazily load the records.
     *
     * @return {@code true} for lazy loading, {@code false} otherwise
     */
    public boolean isLazyLoad() {
        return lazyLoad;
    }

    /**
     * Indicates whether or not the unmarshalling should lazily load the records.
     *
     * @param  lazyLoad {@code true} for lazy loading, {@code false} otherwise
     * @return          Current {@code CsvDataFormat}, fluent API
     */
    public CsvDataFormat setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
        return this;
    }

    /**
     * Indicates whether or not the unmarshalling should produce maps instead of lists.
     *
     * @return {@code true} for maps, {@code false} for lists
     */
    public boolean isUseMaps() {
        return useMaps;
    }

    /**
     * Sets whether or not the unmarshalling should produce maps instead of lists.
     *
     * @param  useMaps {@code true} for maps, {@code false} for lists
     * @return         Current {@code CsvDataFormat}, fluent API
     */
    public CsvDataFormat setUseMaps(boolean useMaps) {
        this.useMaps = useMaps;
        return this;
    }

    /**
     * Indicates whether or not the unmarshalling should produce ordered maps instead of lists.
     *
     * @return {@code true} for maps, {@code false} for lists
     */
    public boolean isUseOrderedMaps() {
        return useOrderedMaps;
    }

    /**
     * Sets whether or not the unmarshalling should produce ordered maps instead of lists.
     *
     * @param  useOrderedMaps {@code true} for maps, {@code false} for lists
     * @return                Current {@code CsvDataFormat}, fluent API
     */
    public CsvDataFormat setUseOrderedMaps(boolean useOrderedMaps) {
        this.useOrderedMaps = useOrderedMaps;
        return this;
    }

    /**
     * Gets the record converter to use. If {@code null} then it will use {@link CsvDataFormat#isUseMaps()} for finding
     * the proper converter.
     *
     * @return Record converter to use
     */
    public CsvRecordConverter<?> getRecordConverter() {
        return recordConverter;
    }

    /**
     * Sets the record converter to use. If {@code null} then it will use {@link CsvDataFormat#isUseMaps()} for finding
     * the proper converter.
     *
     * @param  recordConverter Record converter to use
     * @return                 Current {@code CsvDataFormat}, fluent API
     */
    public CsvDataFormat setRecordConverter(CsvRecordConverter<?> recordConverter) {
        this.recordConverter = recordConverter;
        return this;
    }

    //endregion
    /**
     * Sets whether or not to trim leading and trailing blanks.
     * <p>
     * If {@code null} then the default value of the format used.
     * </p>
     * 
     * @param  trim whether or not to trim leading and trailing blanks. <code>null</code> value allowed.
     * @return      Current {@code CsvDataFormat}, fluent API.
     */
    public CsvDataFormat setTrim(Boolean trim) {
        this.trim = trim;
        return this;
    }

    /**
     * Indicates whether or not to trim leading and trailing blanks.
     * 
     * @return {@link Boolean#TRUE} if leading and trailing blanks should be trimmed. {@link Boolean#FALSE} otherwise.
     *         Could return <code>null</code> if value has NOT been set.
     */
    public Boolean getTrim() {
        return trim;
    }

    /**
     * Sets whether or not to ignore case when accessing header names.
     * <p>
     * If {@code null} then the default value of the format used.
     * </p>
     * 
     * @param  ignoreHeaderCase whether or not to ignore case when accessing header names. <code>null</code> value
     *                          allowed.
     * @return                  Current {@code CsvDataFormat}, fluent API.
     */
    public CsvDataFormat setIgnoreHeaderCase(Boolean ignoreHeaderCase) {
        this.ignoreHeaderCase = ignoreHeaderCase;
        return this;
    }

    /**
     * Indicates whether or not to ignore case when accessing header names.
     * 
     * @return {@link Boolean#TRUE} if case should be ignored when accessing header name. {@link Boolean#FALSE}
     *         otherwise. Could return <code>null</code> if value has NOT been set.
     */
    public Boolean getIgnoreHeaderCase() {
        return ignoreHeaderCase;
    }

    /**
     * Sets whether or not to add a trailing delimiter.
     * <p>
     * If {@code null} then the default value of the format used.
     * </p>
     * 
     * @param  trailingDelimiter whether or not to add a trailing delimiter.
     * @return                   Current {@code CsvDataFormat}, fluent API.
     */
    public CsvDataFormat setTrailingDelimiter(Boolean trailingDelimiter) {
        this.trailingDelimiter = trailingDelimiter;
        return this;
    }

    /**
     * Indicates whether or not to add a trailing delimiter.
     * 
     * @return {@link Boolean#TRUE} if a trailing delimiter should be added. {@link Boolean#FALSE} otherwise. Could
     *         return <code>null</code> if value has NOT been set.
     */
    public Boolean getTrailingDelimiter() {
        return trailingDelimiter;
    }
}
