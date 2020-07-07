require "fileutils"
require "erb"
include ERB::Util
require "json"

class InvestigatorReportGenerator
	def initialize(settings,progress_dialog=nil)
		@settings = {
			"report_directories" => {
				"html_resources" => "resources",
				"exported_files" => "products",
				"item_details" => "items",
			},
			"products" => [],
			"case_information" => {
				"Name" => $current_case.getName,
				"Investigator" => $current_case.getInvestigator,
			},
			"imaging_settings" => {},
			"parallel_export_settings" => NuixPreferences.get_export_parallel_preferences,
			"report_excluded_items" => true,
		}.merge(settings)
		@settings["products"].map{|p|p["subdir"] ||= p["type"]}
		@product_settings_by_type = {}
		@settings["products"].each do |p|
			@product_settings_by_type[p["type"]] = p
		end
		@progress_dialog = progress_dialog
		@templates = {}
		load_templates

		#DEBUG
		require 'pp'
		#pp @settings
		@settings["additional_files"].each{|a|puts a}
	end

	def logMessage(message)
		if !@progress_dialog.nil?
			@progress_dialog.logMessage(message)
		end
	end

	def setCompleted
		if !@progress_dialog.nil?
			@progress_dialog.setCompleted
		end
	end

	def setMainStatus(message)
		if !@progress_dialog.nil?
			@progress_dialog.setMainStatusAndLogIt(message)
		end
	end

	def setSubStatus(message)
		if !@progress_dialog.nil?
			@progress_dialog.setSubStatus(message)
		end
	end

	def setProgressMax(value)
		if !@progress_dialog.nil?
			@progress_dialog.setMainProgress(0,value)
		end
	end

	def setProgressValue(value)
		if !@progress_dialog.nil?
			@progress_dialog.setMainProgress(value)
		end
	end

	def generate_report
		create_directory("")

		if @settings["record_settings"]
			#JSON in JRuby has trouble if map typed with oject, we need to supply hints
			@settings["products"] = @settings["products"].to_a.map{|e|e.to_hash}
			@settings["summary_reports"] = @settings["summary_reports"].to_a.map{|e|e.to_hash}

			File.open(report_path("ReportSettings.json"),"w:utf-8"){|file|file.puts JSON.pretty_generate(@settings)}
		end

		setMainStatus("Creating report foundation...")
		create_directory(@settings["report_directories"]["html_resources"])
		report_html_resources = report_path(@settings["report_directories"]["html_resources"])
		FileUtils::cp(local_resource_path("styles.css"),report_html_resources)
		FileUtils::cp(local_resource_path("bootstrap.min.css"),report_html_resources)
		FileUtils::cp(local_resource_path("jquery.min.js"),File.join(report_html_resources,"jquery.min.js"))
		FileUtils::cp(local_resource_path("bootstrap.min.js"),File.join(report_html_resources,"bootstrap.min.js"))

		#Copy CV file if requested and valid
		if @settings["include_cv_file"]
			setMainStatus("Copying CV file...")
			if java.io.File.new(@settings["cv_file_path"]).exists
				FileUtils::cp(@settings["cv_file_path"],report_path(File.basename(@settings["cv_file_path"])))
			else
				logMessage("User requested CV file be copied but file does not exist, skipping: #{@settings["cv_file_path"]}")
			end
		end

		#Copy Definitions file if requested and valid
		if @settings["include_definitions_file"]
			setMainStatus("Copying definitions file...")
			if java.io.File.new(@settings["definitions_file_path"]).exists
				FileUtils::cp(@settings["definitions_file_path"],report_path(File.basename(@settings["definitions_file_path"])))
			else
				logMessage("User requested CV file be copied but file does not exist, skipping: #{@settings["definitions_file_path"]}")
			end
		end

		#Copy any additional files user may have specified
		@settings["additional_files"].each do |additional_file_info|
			path = additional_file_info["path"]
			title = additional_file_info["title"]
			setMainStatus("Copying additional file '#{title}'...")
			if java.io.File.new(path).exists
				FileUtils::cp(path,report_path(File.basename(path)))
			else
				logMessage("Additional file does not exist, skipping: #{title} : #{path}")
			end
		end

		items = get_items
		@report_items = {}
		@product_exported_items = {}
		items.each{|i|@report_items[i]=true}
		logMessage("Report Items Count: #{items.size}")

		if @settings["products"].size > 0
			export_products(items) 
			create_directory(@settings["report_directories"]["exported_files"])
		end

		generate_case_information_page
		create_directory(@settings["report_directories"]["item_details"])
		generate_item_details(items)
		generate_tag_summaries
		if was_exported?("thumbnail")
			generate_thumbnails_gallery(items)
		end
		generate_index
		setCompleted
	end

	def generate_index
		setMainStatus("Generating index...")
		title = "Report"
		summary_reports = @settings["summary_reports"]
		nav_width = 355
		has_logo = false
		logo_file = local_resource_path("logo.png")
		if java.io.File.new(logo_file).exists
			FileUtils::cp(logo_file,report_path(@settings["report_directories"]["html_resources"]))
			has_logo = true
		else
			logMessage("No logo file located at #{logo_file}")
		end
		b = binding

		File.open(report_path("Index.html"),"w:utf-8") do |file|
			file.puts @templates["index"].result(b)
		end
	end

	def get_map_link(item,profile_fields)
		if profile_fields.any?{|f|f.getName == "Latitude"} && profile_fields.any?{|f|f.getName == "Longitude"}
			lat = profile_fields.select{|f|f.getName == "Latitude"}[0].evaluate(item)
			long += " " + profile_fields.select{|f|f.getName == "Longitude"}[0].evaluate(item)
			if !lat.strip.empty? && !long.strip.empty
				return "<a target=\"blank\" href=\"https://maps.google.com/?q=#{lat_long}\" title=\"Open in google maps, requires internet connectivity.\">Map</a>"
			else
				return ""
			end
		else
			return ""
		end
	end

	def get_native_extension(item)
		return @native_extension_lookup[item.getGuid] || ""
	end

	def get_product_links(item)
		links = []
		@settings["products"].each do |product_info|
			extension = nil
			case product_info["type"]
			when "native"
				extension = get_native_extension(item)
			when "text"
				extension = "txt"
			when "thumbnail"
				next
			when "pdf"
				extension = "pdf"
			end
			#puts "DEBUG: extension is nil: #{product_info["type"]} : #{item.getLocalisedName} | #{item.getType.getKind.getName}"
			exported_dir = @settings["report_directories"]["exported_files"]
			guid = item.getGuid
			guid_subdir = guid[0..2]
			links << "<a class=\"btn btn-default\" href=\"../../#{exported_dir}/#{product_info["subdir"]}/#{guid_subdir}/#{guid}.#{extension}\">#{product_info["type"].capitalize}</a>"
		end
		return links.join
	end

	def get_thumbnail_detail_link(item)
		width=80
		title = "Item&nbsp;Details"
		native_product = @settings["products"].select{|p|p["type"] == "native"}.first
		guid = item.getGuid
		item_type = item.getType
		item_kind = item_type.getKind.getName
		item_mime_type = item_type.getName
		guid_subdir = guid[0..2]
		exported_dir = @settings["report_directories"]["exported_files"]
		if native_product && item_kind == "image" && item_mime_type != "image/tiff"
			extension = item.getCorrectedExtension
			img_tag = "<img src=\"./#{exported_dir}/#{native_product["subdir"]}/#{guid_subdir}/#{guid}.#{extension}\" style=\"width:#{width}px\"/>"
			return "<a href=\"#{get_item_detail_directory(item)}/#{guid}.html\" title=\"#{title}\">#{img_tag}</a>"
		elsif has_thumbnail?(item)
			thumbnail_settings = get_export_settings("thumbnail")
			src = "./#{exported_dir}/#{thumbnail_settings["subdir"]}/#{guid_subdir}/#{guid}.png"
			img_tag = "<img src=\"#{src}\" style=\"width:#{width}px\"/ style=\"width:#{width}px\">"
			return "<a href=\"#{get_item_detail_directory(item)}/#{guid}.html\" title=\"#{title}\">#{img_tag}</a>"
		else
			return "<a href=\"#{get_item_detail_directory(item)}/#{guid}.html\" title=\"#{title}\">#{title}</a>"
		end
	end

	def escape_tag_for_search(tag)
		return tag
		.gsub("\\","\\\\\\") #Escape \
		.gsub("?","\\?") #Escape ?
		.gsub("*","\\*") #Escape *
		.gsub("\"","\\\"") #Escape "
	end

	def escape_filename(input,replacement="_")
		return input.gsub(/[\/\\:\*\?\"<>\|]+/,replacement)
	end

	def generate_tag_summaries
		setMainStatus("Generating Summary Reports...")
		sorter = $utilities.getItemSorter
		@settings["summary_reports"].each do |summary_report|
			tag = summary_report["tag"]
			title = summary_report["title"]
			sort = summary_report["sort"]
			puts("\t* Generating Summary: #{title}")
			escaped_tag = escape_tag_for_search(tag)
			query = "tag:(\"#{escaped_tag}\" OR \"#{escaped_tag}|*\")"
			if !@settings["report_excluded_items"]
				query += " AND has-exclusion:0"
			end
			puts("\t\tSearching: #{query}")
			summary_items = $current_case.search(query)
			summary_report["hit_count"] = summary_items.size
			if summary_items.size > 0
				puts("\t\tSorting Items: #{sort}")
				case sort
					when "Item Position"
						summary_items = sorter.sortItemsByPosition(summary_items)
					when "Item Date Ascending"
						summary_items = sorter.sortItems(summary_items) do |item|
							item_date = item.getDate
							if item_date.nil?
								next 0
							else
								next item_date.getMillis
							end
						end
					when "Item Date Descending"
						summary_items = sorter.sortItems(summary_items) do |item|
							item_date = item.getDate
							if item_date.nil?
								next 0
							else
								next item_date.getMillis * -1
							end
						end
					when "Top Level Item Date Ascending"
						summary_items = sorter.sortItemsByTopLevelItemDate(summary_items)
					when "Top Level Item Date Descending"
						summary_items = sorter.sortItemsByTopLevelItemDateDescending(summary_items)
					when "Profile First Column Value"
						profile = $utilities.getMetadataProfileStore.getMetadataProfile(summary_report["profile"])
						first_column = profile.getMetadata.first
						summary_items = sorter.sortItems(summary_items) do |item|
							value = first_column.evaluate(item)
							value = "" if value.nil?
							next value
						end
					when "Item Name"
						summary_items = sorter.sortItems(summary_items){|item| item.getLocalisedName }
					when "Item Path"
						summary_items = sorter.sortItems(summary_items){|item| item.getPath.map{|i|i.getLocalisedName}.join("/") }
					when "Item Kind"
						summary_items = sorter.sortItems(summary_items){|item| item.getType.getKind.getName }
					when "Mime Type"
						summary_items = sorter.sortItems(summary_items){|item| item.getType.getName }
					when "Recipient Count Ascending"
						summary_items = sorter.sortItems(summary_items) do |item|
							comm = item.getCommunication
							next 0 if comm.nil?
							sum = 0
							sum += comm.getTo.size
							sum += comm.getCc.size
							sum += comm.getBcc.size
							next sum
						end
					when "Recipient Count Descending"
						summary_items = sorter.sortItems(summary_items) do |item|
							comm = item.getCommunication
							next 0 if comm.nil?
							sum = 0
							sum += comm.getTo.size
							sum += comm.getCc.size
							sum += comm.getBcc.size
							next sum * -1
						end
					when "Audited Size Ascending"
						summary_items = sorter.sortItems(summary_items) do |item|
							next item.getAuditedSize || 0
						end
					when "Audited Size Descending"
						summary_items = sorter.sortItems(summary_items) do |item|
							next (item.getAuditedSize || 0) * -1
						end
					when "Audited Size Ascending"
						summary_items = sorter.sortItems(summary_items){|item| item.getAuditedSize || 0 }
					when "Audited Size Descending"
						summary_items = sorter.sortItems(summary_items){|item| (item.getAuditedSize || 0) * -1 }
					when "MD5"
						summary_items = sorter.sortItems(summary_items){|item| item.getDigests.getMd5 || "" }
					when "GUID"
						summary_items = sorter.sortItems(summary_items){|item| item.getGuid }
				end
				summary_report["pages"] = generate_summary(summary_report["title"],summary_items,summary_report["profile"])
			else
				puts("\t\tYielded no hits, skipping summary")
			end
		end
	end

	def generate_summary(title,items,profile_name)
		include_map_link = false #Needs some tweaking possibly still
		profile_fields = $utilities.getMetadataProfileStore.getMetadataProfile(profile_name).getMetadata
		summary_data = {
			"Item Count" => items.size,
			"Total Audited Size" => items.map{|i|i.getAuditedSize}.reject{|s|s<0}.reduce(0,:+),
			"Total File Size" => items.map{|i|i.getFileSize||0}.reject{|s|s<0}.reduce(0,:+),
		}

		current_page_number = 0
		total_pages = (items.size.to_f / @settings["records_per_summary_report"].to_f).ceil
		items.each_slice(@settings["records_per_summary_report"]) do |slice_items|
			current_page_number += 1
			b = binding

			setSubStatus("(#{current_page_number} / #{total_pages}) #{title} ")
			setProgressMax(slice_items.size)

			prev_page = nil
			if current_page_number > 1
				prev_page = "Summary_#{escape_filename(title)}_#{(current_page_number-1).to_s.rjust(4,"0")}.html"
			end
			next_page = nil
			if current_page_number != total_pages
				next_page = "Summary_#{escape_filename(title)}_#{(current_page_number+1).to_s.rjust(4,"0")}.html"
			end

			File.open(report_path("Summary_#{escape_filename(title)}_#{current_page_number.to_s.rjust(4,"0")}.html"),"w:utf-8") do |file|
				file.puts @templates["summary_report"].result(b)
			end
		end

		return current_page_number
	end

	def get_sanitized_properties(item)
		result = {}
		item_properties = item.getProperties
		metadata_profile = $utilities.metadata_profile_store.create_metadata_profile
		item_properties.each do |key,value|
			metadata_profile = metadata_profile.add_metadata("PROPERTY", key)
		end
		metadata_profile.metadata.each do |metadata_item|
			result[metadata_item.name] = metadata_item.evaluate(item)
		end
		return result
	end

	def generate_item_details(items)
		setMainStatus("Generating per Item Details...")
		setProgressMax(items.size)

		profile_name = ""
		profile_fields = []
		if @settings["item_details"]["include_profile"]
			profile_name = @settings["item_details"]["include_profile"]
			profile_fields = $utilities.getMetadataProfileStore.getMetadataProfile(@settings["item_details"]["include_profile"]).getMetadata
		end

		last_progress = Time.now
		items.each_with_index do |item,item_index|
			create_directory(get_item_detail_directory(item))
			if (Time.now - last_progress) > 1
				setProgressValue(item_index+1)
				setSubStatus("#{item_index+1}/#{items.size}")
				last_progress = Time.now
			end
			#Data for template
			title = item.getLocalisedName
			data = {
				"Custodian" => item.getCustodian,
				"File Size" => (item.getFileSize || 0).with_commas,
				"File Type" => item.getType.getLocalisedName,
				"GUID" => item.getGuid,
				"Item Date" => item.getDate,
				"MD5 Digest" => item.getDigests.getMd5,
				"Path Name" => "/"+item.getPathNames.to_a[0...-1].join("/"),
				"Shannon Entropy" => item.getShannonEntropy,
			}

			email_data = {}
			has_communication = false
			if item.isKind("email")
				communication = item.getCommunication
				if !communication.nil?
					has_communication = true
					email_data["Subject"] = item.getProperties["Subject"]
					email_data["From"] = communication.getFrom.map{|a|a.toDisplayString}
					email_data["To"] = communication.getTo.map{|a|a.toDisplayString}
					email_data["CC"] = communication.getCc.map{|a|a.toDisplayString}
					email_data["BCC"] = communication.getBcc.map{|a|a.toDisplayString}
				end
			end

			comments = ""
			if @settings["item_details"]["include_comments"]
				comments = item.getComment
			end

			properties = {}
			if @settings["item_details"]["include_properties"]
				properties = get_sanitized_properties(item).sort_by{|n,v|n}
			end

			tags = []
			if @settings["item_details"]["include_tags"]
				tags = item.getTags.sort
			end

			custom_metadata = []
			if @settings["item_details"]["include_custom_metadata"]
				custom_metadata = item.getCustomMetadata.sort_by{|n,v|n}
			end

			profile_values = {}
			if @settings["item_details"]["include_profile"]
				profile_fields.each do |field|
					profile_values[field.getName] = field.evaluate(item)
				end
			end

			b = binding

			detail_path = report_path(File.join(get_item_detail_directory(item),"#{item.getGuid}.html"))
			File.open(detail_path,"w:utf-8") do |file|
				majority_html = @templates["item_detail"].result(b)
				# Attempt to trim some fat
				majority_html = majority_html.gsub(/\r\n/,"\n")
				majority_html = majority_html.gsub(/^\s+</,"<")
				majority_html = majority_html.gsub(/>\n</,"><")
				
				file.write majority_html

				if @settings["item_details"]["include_text"]
					# Write beginning of text PRE block
					file.write "<h3 class=\"page-header\">Text</h3>"
					file.write "<pre style=\"white-space:pre-wrap;\">"

					text_object = item.getTextObject
					if NuixConnection.getCurrentNuixVersion.isAtLeast("8.6")
						buffered_reader = nil
						begin
							text_object.usingText do |reader|
								buffered_reader = BufferedReader.new(reader)
								while true
									line = buffered_reader.readLine
									break if line.nil?
									# Write text line by line, escaping HTML
									# special characters along the way
									file.write html_escape(line)+"\n"
								end
							end
						rescue Exception => exc
							logMessage("ERROR while adding text to item detail: #{exc.message}\n#{exc.backtrace.join("\n")}")
						ensure
							if !buffered_reader.nil?
								buffered_reader.close
							end
						end
					else
						# We are not in Nuix 8.6+ so we have to use the older more perilous way
						# where we can only get the whole item text as a single string which may
						# cause a string to allocate which is bigger than max Java array length
						begin
							file.write text_object.toString({:lineSeparator=>"\n"})
						rescue Exception => exc
							logMessage("ERROR while adding text to item detail: #{exc.message}\n#{exc.backtrace.join("\n")}")
						end
					end

					# Write close of text PRE block
					file.write "</pre>"
				end

				# Leaving this off template, since we may need to include item text in file separately
				# due to it blowing out Java array limit when template apparently does a join at the end
				# see here: https://github.com/jruby/jruby/issues/4704
				file.puts "</body></html>"
			end
		end
	end

	def generate_case_information_page
		setMainStatus("Generating Case Information Report...")
		#Data for template
		title = "Case Information"
		case_information = @settings["case_information"]
		case_information["Report Date"] = Time.now.strftime("%Y%m%d %H:%M:%S")
		b = binding

		File.open(report_path("CaseInformation.html"),"w:utf-8") do |file|
			file.puts @templates["case_information"].result(b)
		end
	end

	def generate_thumbnails_gallery(items)
		setMainStatus("Generating Thumbnails Gallery...")
		#Data for template
		title = "Thumbnails"
		b = binding
		File.open(report_path("ThumbnailsGallery.html"),"w:utf-8") do |file|
			file.puts @templates["thumbnails_gallery"].result(b)
		end
	end

	def load_templates
		names = [
			"case_information",
			"item_detail",
			"summary_report",
			"index",
			"thumbnails_gallery",
		]
		names.each do |template_name|
			template_path = File.join(File.dirname(__FILE__),"erb_templates","#{template_name}_template.erb")
			@templates[template_name] = ERB.new(File.read(template_path), nil, '-')
		end
	end

	def get_export_settings(product_name)
		return @product_settings_by_type[product_name]
	end

	def was_exported?(product_name)
		return get_export_settings(product_name).nil? == false
	end

	def has_thumbnail?(item)
		if !was_exported?("thumbnail")
			return false
		else
			thumbnail_settings = get_export_settings("thumbnail")
			subdir = thumbnail_settings["subdir"]
			export_dir = report_path(@settings["report_directories"]["exported_files"])
			image_path = File.join(export_dir,subdir,item.getGuid[0..2],"#{item.getGuid}.png")
			return java.io.File.new(image_path).exists
		end
	end

	def export_products(items)
		logMessage("Exporting products...")
		logMessage(@settings["products"].map{|p|" - #{p["type"]}"}.join("\n"))
		setMainStatus("Exporting...")
		setProgressMax(items.size)
		exporter = $utilities.createBatchExporter(report_path(@settings["report_directories"]["exported_files"]))
		native_product_info = nil
		@settings["products"].each do |product_info|
			product_settings = {
				"naming" => "guid",
				"path" => product_info["subdir"],
			}
			if product_info["type"] == "pdf"
				logMessage("PDF regenerateStored: #{product_info["regenerateStored"]}")
				product_settings["regenerateStored"] = product_info["regenerateStored"]
			elsif product_info["type"] == "native"
				native_product_info = product_info
			end
			exporter.addProduct(product_info["type"],product_settings)
		end
		#Can only call this if the the licence has the feature "EXPORT_LEGAL"
		if $utilities.getLicence.hasFeature("EXPORT_LEGAL")
			exporter.setNumberingOptions({"createProductionSet" => false})
		end

		exporter.whenItemEventOccurs do |event|
			setSubStatus(event.getStage)
			setProgressValue(event.getStageCount)
		end

		exporter.setParallelProcessingSettings(@settings["parallel_export_settings"])

		puts "DEBUG: IMAGING SETTINGS: #{@settings["imaging_settings"]}"
		exporter.setImagingOptions(@settings["imaging_settings"])
		
		# Filter any items which are in a summary deemed "Export No Products"
		exportable_items = items
		export_disabled_summaries = @settings["summary_reports"].select{|sr|sr["disable_export"] == true}
		no_export_tags = export_disabled_summaries.map do |e|
			tag = e["tag"]
			escaped_tag = escape_tag_for_search(tag)
			next "\"#{escaped_tag}\""
		end
		no_export_tags += export_disabled_summaries.map do |e|
			tag = e["tag"]
			escaped_tag = escape_tag_for_search(tag)
			next "\"#{escaped_tag}|*\""
		end
		no_export_tags = no_export_tags.uniq
		if no_export_tags.size > 0
			logMessage("Filtering out items belonging to summaries for which no products are to be exported...")
			logMessage(no_export_tags.join("\n"))
			no_export_items = $current_case.searchUnsorted("tag:(#{no_export_tags.join(" OR ")})")
			logMessage("No Export Items: #{no_export_items.size}")
			iutil = $utilities.getItemUtility
			exportable_items = iutil.difference(items,no_export_items)
		end
		exportable_items.each{|i|@product_exported_items[i]=true}
		logMessage("Exportable Items: #{exportable_items.size}")
		logMessage("Beginning export...")
		exporter.exportItems(exportable_items.to_a)
		logMessage("Product Export Completed")

		# Need to build XREF GUID => extension since it can be near impossible to guess the extension
		# exported for some natives
		if !native_product_info.nil?
			logMessage("Building native extension lookup...")
			require 'pathname'
			@native_extension_lookup = {}
			export_root = report_path(@settings["report_directories"]["exported_files"])
			native_subdir = native_product_info["subdir"]
			native_export_directory = File.join(export_root,native_subdir)
			Dir.glob(File.join(native_export_directory,"**","*.*")).each do |native_file_path|
				guid = File.basename(native_file_path,".*")
				extension = File.extname(native_file_path)
				@native_extension_lookup[guid] = extension.gsub(/^\./,"")
			end
			logMessage("Recorded #{@native_extension_lookup.size} extensions")
		end
	end

	def get_items
		setMainStatus("Obtaining Report Items...")
		tags = @settings["summary_reports"].map do |e|
			tag = e["tag"]
			escaped_tag = escape_tag_for_search(tag)
			next "\"#{escaped_tag}\""
		end
		tags += @settings["summary_reports"].map do |e|
			tag = e["tag"]
			escaped_tag = escape_tag_for_search(tag)
			next "\"#{escaped_tag}|*\""
		end
		logMessage("Report Excluded Items: #{@settings["report_excluded_items"]}")
		query = "tag:(#{tags.uniq.join(" OR ")})"
		if !@settings["report_excluded_items"]
			query += " AND has-exclusion:0"
		end
		logMessage("Searching: #{query}")
		items = $current_case.search(query)
		return items
	end

	def create_directory(relative_path)
		directory = java.io.File.new(report_path(relative_path))
		if !directory.exists
			puts "Creating directory: #{directory.getPath}"
			directory.mkdirs
		end
	end

	def report_path(relative_path)
		return File.join(@settings["output_directory"],relative_path)
	end

	def local_path(relative_path)
		return File.join(File.dirname(__FILE__),relative_path)
	end

	def local_resource_path(relative_path)
		return File.join(File.dirname(__FILE__),"resources",relative_path)
	end

	def get_item_detail_directory(item)
		return File.join(@settings["report_directories"]["item_details"],item.getGuid[0..2])
	end
end