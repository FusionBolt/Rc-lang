module Rc
  module Helper
    def under_score_class_name(obj)
      s = obj.class.to_s
      if s.include? '::'
        underscore(s.split('::')[1])
      else
        underscore(s)
      end
    end

    def underscore(str)
      str.gsub(/::/, '/').
        gsub(/([A-Z]+)([A-Z][a-z])/, '\1_\2').
        gsub(/([a-z\d])([A-Z])/, '\1_\2').
        tr("-", "_").
        downcase
    end
    module_function :under_score_class_name
    module_function :underscore
  end
end
