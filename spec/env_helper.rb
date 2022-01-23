require './define'

def get_kernel_methods(env)
  env.class_table[Rc::Define::GlobalObject].instance_methods
end