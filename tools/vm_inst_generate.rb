require './ir/vm/generator'

File.open('../RCVM/instructions.hpp', 'w').write(gen_inst_src)
