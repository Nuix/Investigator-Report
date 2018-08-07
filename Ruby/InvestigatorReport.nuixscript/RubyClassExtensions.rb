#This file re-opens some standard Ruby classes and extends them
#with useful methods
class Numeric
	def to_gb(decimal_places=3)
		gb_f = self.to_f / (1000.0 ** 3)
		if decimal_places == 0
			return gb_f.to_i
		else
			return gb_f.round(decimal_places)
		end
	end

	def to_mb(decimal_places=3)
		gb_f = self.to_f / (1000.0 ** 2)
		if decimal_places == 0
			return gb_f.to_i
		else
			return gb_f.round(decimal_places)
		end
	end

	def to_kb(decimal_places=3)
		gb_f = self.to_f / (1000.0)
		if decimal_places == 0
			return gb_f.to_i
		else
			return gb_f.round(decimal_places)
		end
	end

	def to_filesize(decimal_places=3)
		if self >= (1000 ** 3)
			return "#{self.to_gb(decimal_places).with_commas} GB"
		elsif self >= (1000 ** 2)
			return "#{self.to_mb(decimal_places).with_commas} MB"
		elsif self >= (1000)
			return "#{self.to_kb(decimal_places).with_commas} KB"
		else
			return "#{self.with_commas} B"
		end
	end

	def to_elapsed
		Time.at(self).gmtime.strftime("%H:%M:%S")
	end
end

class Integer
	def with_commas
		return self.to_s.reverse.gsub(/...(?=.)/,'\&,').reverse
	end
end

class Float
	def with_commas
		parts = self.to_s.split('.')
		parts[0] = parts[0].to_s.reverse.gsub(/...(?=.)/,'\&,').reverse
		return parts.join(".")
	end
end