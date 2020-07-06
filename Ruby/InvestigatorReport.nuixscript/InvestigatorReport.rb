# Menu Title: Investigator Report
# Needs Case: true

defaults = {
	:export_directory => "C:\\InvestigatorReports".gsub(/[\/\\]$/,File::SEPARATOR),
	:worker_count => nil,
	:worker_memory => 1024,
	:worker_temp => "C:\\NuixWorkerTemp".gsub(/[\/\\]$/,File::SEPARATOR),
	:report_title_prefix => "Report ",
	:report_excluded_items => true,
	:item_details => {
		"export_natives" => false,
		"export_pdfs" => false,
		"export_text" => false,
		"export_thumbnails" => false,
		"image_spreadsheets" => false,
		"regenerate_stored_pdfs" => false,
		"include_comments" => false,
		"include_properties" => true,
		"include_tags" => false,
		"include_custom_metadata" => false,
		"include_profile" => false,
		"included_profile_name" => "Default metadata profile",
		"include_text" => true,
	}
}

script_directory = File.dirname(__FILE__).gsub(/[\/\\]$/,File::SEPARATOR)

script_directory = File.dirname(__FILE__)
require File.join(script_directory,"Nx.jar")
java_import "com.nuix.nx.NuixConnection"
java_import "com.nuix.nx.LookAndFeelHelper"
java_import "com.nuix.nx.dialogs.ChoiceDialog"
java_import "com.nuix.nx.dialogs.TabbedCustomDialog"
java_import "com.nuix.nx.dialogs.CommonDialogs"
java_import "com.nuix.nx.dialogs.ProgressDialog"
java_import "com.nuix.nx.dialogs.ProcessingStatusDialog"
java_import "com.nuix.nx.digest.DigestHelper"
java_import "com.nuix.nx.controls.models.Choice"

LookAndFeelHelper.setWindowsIfMetal
NuixConnection.setUtilities($utilities)
NuixConnection.setCurrentNuixVersion(NUIX_VERSION)

load File.join(script_directory,"RubyClassExtensions.rb")
load File.join(script_directory,"NuixPreferences.rb")
load File.join(script_directory,"InvestigatorReportGenerator.rb")
require File.join(script_directory,"InvestigatorReportGUI.jar")

java_import com.nuix.investigatorreport.ReportSettingsDialog
java_import java.io.BufferedReader

# Allows you to configure the default value of "Records per Page" setting
# Note that value must be >= 10
ReportSettingsDialog.setDefaultRecordsPerPage(2000)

tags = $current_case.getAllTags.sort
profiles = $utilities.getMetadataProfileStore.getMetadataProfiles.map{|p|p.getName}.sort
dialog = ReportSettingsDialog.new(tags,profiles,defaults[:report_title_prefix])
dialog.setOutputDirectory(File.join(defaults[:export_directory],Time.now.strftime("%Y%m%d_%H-%M-%S")))
export_parallel_preferences = NuixPreferences.get_export_parallel_preferences
dialog.getSpinnerWorkerCount.setValue(defaults[:worker_count] || export_parallel_preferences["workerCount"])
dialog.getSpinnerWorkerMemory.setValue(defaults[:worker_memory] || export_parallel_preferences["workerMemory"])
dialog.getTxtWorkerTemp.setText(defaults[:worker_temp] || export_parallel_preferences["workerTemp"])
dialog.setItemDetailSettings(defaults[:item_details])
dialog.getChckbxReportExcludedItems.setSelected(defaults[:report_excluded_items])
dialog.setVisible(true)
if dialog.getDialogResult == true
	report_settings = dialog.getSettings

	ProgressDialog.forBlock do |progress_dialog|
		progress_dialog.setAbortButtonVisible(false)
		progress_dialog.onMessageLogged do |message|
			puts message
		end
		progress_dialog.setTitle("Investigator Report")
		reporter = InvestigatorReportGenerator.new(report_settings,progress_dialog)
		reporter.generate_report
	end
end