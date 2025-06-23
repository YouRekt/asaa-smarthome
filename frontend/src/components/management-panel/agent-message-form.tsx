import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { DialogFooter } from "@/components/ui/dialog";
import {
	Form,
	FormControl,
	FormDescription,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { useStomp } from "@/hooks/use-stomp";
import {
	performatives,
	performativeSchema,
	type Agent,
	type Performative,
} from "@/hooks/use-store";
import { cn, formatDate } from "@/lib/utils";
import { zodResolver } from "@hookform/resolvers/zod";
import { Send } from "lucide-react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod/v4";

const FormSchema = z.object({
	aid: z.string(),
	performative: performativeSchema,
	conversationId: z.string(),
	message: z.string(),
});

const AgentMessageForm = ({ agent }: { agent: Agent }) => {
	const { publish } = useStomp();

	const form = useForm<z.infer<typeof FormSchema>>({
		resolver: zodResolver(FormSchema),
		defaultValues: {
			aid: agent.aid,
			performative: "INFORM",
			conversationId: "",
			message: "",
		},
	});

	function onSubmit(data: z.infer<typeof FormSchema>) {
		try {
			publish("/app/agent-message", data);

			// Get performative color (borrowed from Message component)
			const getPerformativeColor = (performative: Performative) => {
				switch (performative) {
					case "INFORM":
						return "bg-blue-600/10 text-blue-800 dark:text-blue-600";
					case "REQUEST":
						return "bg-orange-600/10 text-orange-800 dark:text-orange-600";
					case "CFP":
						return "bg-purple-600/10 text-purple-800 dark:text-purple-600";
					case "AGREE":
						return "bg-green-600/10 text-green-800 dark:text-green-600";
					case "REFUSE":
						return "bg-red-600/10 text-red-800 dark:text-red-600";
					case "CONFIRM":
						return "bg-teal-600/10 text-teal-800 dark:text-teal-600";
					case "REJECT_PROPOSAL":
						return "bg-rose-600/10 text-rose-800 dark:text-rose-600";
					case "ACCEPT_PROPOSAL":
						return "bg-emerald-600/10 text-emerald-800 dark:text-emerald-600";
					case "PROPOSE":
						return "bg-yellow-600/10 text-yellow-800 dark:text-yellow-600";
					default:
						return "bg-zinc-500/10 text-zinc-800 dark:text-zinc-600";
				}
			};

			// Custom toast with better styling
			toast.message(
				<div className="w-full">
					{/* Header similar to Message component */}
					<div className="flex items-center justify-between mb-2">
						<div className="flex items-center gap-2 flex-wrap">
							<span className="font-medium text-sm">
								Message Sent to:
							</span>
							<Badge
								className={cn(
									"text-xs flex-shrink-0",
									agent.type === "sensor"
										? "bg-blue-500/20 text-blue-800 dark:text-blue-400"
										: "bg-green-500/20 text-green-800 dark:text-green-400"
								)}
							>
								{agent.name}
							</Badge>
							<Badge
								className={cn(
									"text-xs flex-shrink-0",
									getPerformativeColor(
										data.performative as Performative
									)
								)}
							>
								{data.performative}
							</Badge>
						</div>
						<span className="text-xs text-muted-foreground">
							{formatDate(new Date())}
						</span>
					</div>

					{/* Conversation ID if provided */}
					{data.conversationId && (
						<div className="text-xs text-muted-foreground mb-2">
							Conversation: {data.conversationId}
						</div>
					)}

					{/* Message content if provided */}
					{data.message && (
						<div className="text-sm">
							<div className="bg-muted/50 p-2 rounded text-xs font-mono break-words">
								{data.message}
							</div>
						</div>
					)}
				</div>,
				{
					duration: 5000,
					className: "max-w-md",
				}
			);

			form.reset({
				aid: agent.aid,
				performative: "INFORM",
				conversationId: "",
				message: "",
			});
		} catch {
			toast.error("Failed to send message", {
				description: "Please check your connection and try again.",
			});
		}
	}

	return (
		<Form {...form}>
			<form
				onSubmit={form.handleSubmit(onSubmit)}
				className="flex flex-col gap-6 mt-4"
			>
				<FormField
					control={form.control}
					name="performative"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Performative</FormLabel>
							<FormDescription>
								Select the performative of the message
							</FormDescription>
							<Select
								onValueChange={field.onChange}
								defaultValue={field.value}
							>
								<FormControl>
									<SelectTrigger>
										<SelectValue placeholder="Select message type" />
									</SelectTrigger>
								</FormControl>
								<SelectContent>
									{performatives.map((performative) => (
										<SelectItem
											key={performative}
											value={performative}
										>
											{performative}
										</SelectItem>
									))}
								</SelectContent>
							</Select>
							<FormMessage />
						</FormItem>
					)}
				/>
				<FormField
					control={form.control}
					name="conversationId"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Conversation ID</FormLabel>
							<FormControl>
								<Input
									placeholder="eg. power-relief, trigger"
									{...field}
								/>
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>
				<FormField
					control={form.control}
					name="message"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Message</FormLabel>
							<FormControl>
								<Input
									placeholder="Message content (optional)"
									{...field}
								/>
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>
				<DialogFooter>
					<Button type="submit" className="flex items-center gap-2">
						<Send className="h-4 w-4" />
						Send
					</Button>
				</DialogFooter>
			</form>
		</Form>
	);
};

export default AgentMessageForm;
