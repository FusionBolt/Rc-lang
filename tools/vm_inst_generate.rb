require './ir/vm/generator'

File.open('/home/homura/Code/RCVM/instructions.hpp', 'w').write(gen_inst_src)
