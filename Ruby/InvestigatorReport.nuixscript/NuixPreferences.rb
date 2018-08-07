require "win32/registry"

#This class demonstrates accessing the registry to get preferences used by the GUI
class NuixPreferences
	class << self
		@@processing_reg_path = "Software\\JavaSoft\\Prefs\\com\\nuix\\investigator\\wizard\\parallel/Processing/Settings64"
		@@export_reg_path = "Software\\JavaSoft\\Prefs\\com\\nuix\\investigator\\export\\legal\\parallel/Processing/Settings64"
		
		def get_parallel_preferences(root)
			#Create result object with defaults
			result = {
				"workerCount" => 4,
				"workerMemory" => 1024,
				"workerTemp" => "c:\\WorkerTemp",
				"embedBroker" => true,
				"brokerMemory" => 768,
			}
			
			begin
				Win32::Registry::HKEY_CURRENT_USER.open(root) do |reg|
					begin
						result["workerCount"] = reg["worker-count"].to_i
					rescue Exception => e
					end

					begin
						result["workerMemory"] = reg["worker-memory"].to_i
					rescue Exception => e
					end

					begin
						#Need to unescape registry value for path
						result["workerTemp"] = reg["worker-temp-directory"].gsub(/\/([A-Z\/])/,"\\1").gsub("/","\\")
					rescue Exception => e
					end

					begin
						result["embedBroker"] = reg["embed-broker"] == "true"
					rescue Exception => e
					end

					begin
						result["brokerMemory"] = reg["broker-memory"].to_i
					rescue Exception => e
					end
				end
			rescue Exception => e
				#Apparently could not locate registry key root if we are here
			end

			return result
		end

		def get_processing_parallel_preferences
			return get_parallel_preferences(@@processing_reg_path)
		end

		def get_export_parallel_preferences
			return get_parallel_preferences(@@export_reg_path)
		end
	end
end